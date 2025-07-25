package org.shuavideos.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shuavideos.config.LocalCache;
import org.shuavideos.config.QiNiuConfig;
import org.shuavideos.constant.AuditStatus;
import org.shuavideos.constant.RedisConstant;
import org.shuavideos.entity.File;
import org.shuavideos.entity.task.VideoTask;
import org.shuavideos.entity.user.User;
import org.shuavideos.entity.video.Type;
import org.shuavideos.entity.video.Video;
import org.shuavideos.entity.video.VideoShare;
import org.shuavideos.entity.video.VideoStar;
import org.shuavideos.entity.vo.BasePage;
import org.shuavideos.entity.vo.HotVideo;
import org.shuavideos.entity.vo.UserModel;
import org.shuavideos.entity.vo.UserVO;
import org.shuavideos.exception.BaseException;
import org.shuavideos.holder.UserHolder;
import org.shuavideos.mapper.video.VideoMapper;
import org.shuavideos.service.FeedService;
import org.shuavideos.service.FileService;
import org.shuavideos.service.InterestPushService;
import org.shuavideos.service.audit.VideoPublishAuditServiceImpl;
import org.shuavideos.service.user.FavoritesService;
import org.shuavideos.service.user.FollowService;
import org.shuavideos.service.user.UserService;
import org.shuavideos.service.video.TypeService;
import org.shuavideos.service.video.VideoService;
import org.shuavideos.service.video.VideoShareService;
import org.shuavideos.service.video.VideoStarService;
import org.shuavideos.util.FileUtil;
import org.shuavideos.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {



    @Autowired
    private FileService fileService;

    @Autowired
    private FavoritesService favoritesService;

    @Autowired
    private InterestPushService interestPushService;
    @Autowired
    private UserService userService;

    @Autowired
    private VideoStarService videoStarService;

    @Autowired
    private TypeService typeService;
    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private FollowService followService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private VideoShareService videoShareService;


    final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    VideoPublishAuditServiceImpl videoPublishAuditService;

    @Override
    public IPage<Video> listByUserIdOpenVideo(Long userId, BasePage basePage) {
        if (userId == null) {
            return new Page<>();
        }
        final IPage<Video> page = page(basePage.page(), new LambdaQueryWrapper<Video>().eq(Video::getUserId, userId).eq(Video::getAuditStatus, AuditStatus.SUCCESS).orderByDesc(Video::getGmtCreated));
        final List<Video> videos = page.getRecords();
        setUserVoAndUrl(videos);
        return page;
    }

    @Override
    public Video getVideoById(Long videoId, Long userId) {
        final Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId));
        if (video == null) throw new BaseException("指定视频不存在");

        // 私密则返回为空
        if (video.getOpen()) return new Video();
        setUserVoAndUrl(Collections.singleton(video));
        // 当前视频用户自己是否有收藏/点赞过等信息
        // 这里需要优化 如果这里开线程获取则系统g了(因为这里的场景不适合) -> 请求数很多
        // 正确做法: 视频存储在redis中，点赞收藏等行为异步放入DB, 定时任务扫描DB中不重要更新redis
        video.setStart(videoStarService.starState(videoId, userId));
        video.setFavorites(favoritesService.favoritesState(videoId, userId));
        video.setFollow(followService.isFollows(video.getUserId(), userId));
        return video;
    }

    @Override
    public boolean favoritesVideo(Long fId, Long vId, Long uId) {
        final Video video = getById(vId);
        if (video == null) {
            throw new BaseException("指定视频不存在");
        }
        final boolean favorites = favoritesService.favorites(fId, vId, uId);
        updateFavorites(video, favorites ? 1L : -1L);

        if(favorites) {
            final List<String> labels = video.buildLabel();
            final UserModel userModel = UserModel.buildUserModel(labels, vId, 2.0);
            //  用户兴趣模型修改
            interestPushService.updateUserModel(userModel);
        }
        return favorites;
    }

    @Override
    public boolean startVideo(Long videoId) {
        final Video video = getById(videoId);
        if (video == null) throw new BaseException("指定视频不存在");

        final VideoStar videoStar = new VideoStar();
        videoStar.setVideoId(videoId);
        videoStar.setUserId(UserHolder.get());
        final boolean result = videoStarService.starVideo(videoStar);
        updateStar(video, result ? 1L : -1L);
        // 获取标签
        final List<String> labels = video.buildLabel();

        final UserModel userModel = UserModel.buildUserModel(labels, videoId, 1.0);
        interestPushService.updateUserModel(userModel);

        return result;
    }

    @Override
    public Collection<Video> getVideoByTypeId(Long typeId) {
        if (typeId == null) return Collections.EMPTY_LIST;
        final Type type = typeService.getById(typeId);
        if (type == null) return Collections.EMPTY_LIST;

        Collection<Long> videoIds = interestPushService.listVideoIdByTypeId(typeId);
        if (ObjectUtils.isEmpty(videoIds)) {
            return Collections.EMPTY_LIST;
        }
        final Collection<Video> videos = listByIds(videoIds);

        setUserVoAndUrl(videos);
        return videos;
    }

    @Override
    public List<Video> selectNDaysAgeVideo(long id, int days, int limit) {
        return videoMapper.selectNDaysAgeVideo(id, days, limit);
    }

    @Override
    public List<HotVideo> hotRank() {
        final Set<ZSetOperations.TypedTuple<Object>> zSet = redisTemplate.opsForZSet().reverseRangeWithScores(RedisConstant.HOT_RANK, 0, -1);
        final ArrayList<HotVideo> hotVideos = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> objectTypedTuple : zSet) {
            final HotVideo hotVideo;
            try {
                hotVideo = objectMapper.readValue(objectTypedTuple.getValue().toString(), HotVideo.class);
                hotVideo.setHot((double) objectTypedTuple.getScore().intValue());
                hotVideo.hotFormat();
                hotVideos.add(hotVideo);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return hotVideos;
    }

    @Override
    public Collection<Video> listHotVideo() {

        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);

        final HashMap<String, Integer> map = new HashMap<>();
        // 优先推送今日的
        map.put(RedisConstant.HOT_VIDEO + today, 10);
        map.put(RedisConstant.HOT_VIDEO + (today - 1), 3);
        map.put(RedisConstant.HOT_VIDEO + (today - 2), 2);

        // 游客不用记录
        // 获取今天日期
        final List<Long> hotVideoIds = redisCacheUtil.pipeline(connection -> {
            map.forEach((k, v) -> {
                connection.sRandMember(k.getBytes(), v);
            });
            return null;
        });
        if (ObjectUtils.isEmpty(hotVideoIds)) {
            return Collections.EMPTY_LIST;
        }
        final ArrayList<Long> videoIds = new ArrayList<>();
        // 会返回结果有null，做下校验
        for (Object videoId : hotVideoIds) {
            if (!ObjectUtils.isEmpty(videoId)) {
                videoIds.addAll((List) videoId);
            }
        }
        if (ObjectUtils.isEmpty(videoIds)){
            return Collections.EMPTY_LIST;

        }
        final Collection<Video> videos = listByIds(videoIds);
        // 和浏览记录做交集? 不需要做交集，热门视频和兴趣推送不一样
        setUserVoAndUrl(videos);
        return videos;
    }

    @Override
    public Collection<Video> listSimilarVideo(Video video) {
        if (ObjectUtils.isEmpty(video) || ObjectUtils.isEmpty(video.getLabelNames())) return Collections.EMPTY_LIST;
        final List<String> labels = video.buildLabel();
        final ArrayList<String> labelNames = new ArrayList<>();
        labelNames.addAll(labels);
        labelNames.addAll(labels);
        final Set<Long> videoIds = (Set<Long>) interestPushService.listVideoIdByLabels(labelNames);

        Collection<Video> videos = new ArrayList<>();

        // 去重
        videoIds.remove(video.getId());

        if (!ObjectUtils.isEmpty(videoIds)) {
            videos = listByIds(videoIds);
            setUserVoAndUrl(videos);
        }
        return videos;
    }

    @Override
    public void updateFollowFeed(Long userId) {
        // 获取所有关注的人
        final Collection<Long> followIds = followService.getFollow(userId, null);
        feedService.updateFollowFeed(userId, followIds);
    }

    @Override
    public Collection<Video> followFeed(Long userId, Long lastTime) {
//        return Collections.emptyList();
        // 是否存在
        Set<Long> set = redisTemplate.opsForZSet()
                .reverseRangeByScore(RedisConstant.IN_FOLLOW + userId,
                        0, lastTime == null ? new Date().getTime() : lastTime, lastTime == null ? 0 : 1, 5);
        if (ObjectUtils.isEmpty(set)) {
            // 可能只是缓存中没有了,缓存只存储7天内的关注视频,继续往后查看关注的用户太少了,不做考虑 - feed流必然会产生的问题
            return Collections.EMPTY_LIST;
        }

        // 这里不会按照时间排序，需要手动排序
        final Collection<Video> videos = list(new LambdaQueryWrapper<Video>().in(Video::getId, set).orderByDesc(Video::getGmtCreated));

        setUserVoAndUrl(videos);
        return videos;
    }

    @Override
    public void publishVideo(Video video) {

        final Long userId = UserHolder.get();
        Video oldVideo = null;
        // 不允许修改视频
        final Long videoId = video.getId();
        if (videoId != null) {
            // url不能一致
            oldVideo = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, videoId).eq(Video::getUserId, userId));
            if (!(video.buildVideoUrl()).equals(oldVideo.buildVideoUrl()) || !(video.buildCoverUrl().equals(oldVideo.buildCoverUrl()))) {
                throw new BaseException("不能更换视频源,只能修改视频信息");
            }
        }
        // 判断对应分类是否存在
        final Type type = typeService.getById(video.getTypeId());
        if (type == null) {
            throw new BaseException("分类不存在");
        }
        // 校验标签最多不能超过5个
        if (video.buildLabel().size() > 5) {
            throw new BaseException("标签最多只能选择5个");
        }

        // 修改状态
        video.setAuditStatus(AuditStatus.PROCESS);
        video.setUserId(userId);

        boolean isAdd = videoId == null ? true : false;

        // 校验
        video.setYv(null);

        if (!isAdd) {
            video.setVideoType(null);
            video.setLabelNames(null);
            video.setUrl(null);
            video.setCover(null);
        } else {

            // 如果没设置封面,我们帮他设置一个封面
            if (ObjectUtils.isEmpty(video.getCover())) {
                video.setCover(fileService.generatePhoto(video.getUrl(), userId));
            }

            video.setYv("YV" + UUID.randomUUID().toString().replace("-", "").substring(8));
        }

        // 填充视频时长 (若上次发布视频不存在Duration则会尝试获取)
        if (isAdd || !StringUtils.hasLength(oldVideo.getDuration())) {
            final String uuid = UUID.randomUUID().toString();
            LocalCache.put(uuid, true);
            try {
                Long url = video.getUrl();
                if (url == null || url == 0) url = oldVideo.getUrl();
                final String fileKey = fileService.getById(url).getFileKey();
                final String duration = FileUtil.getVideoDuration(QiNiuConfig.CNAME + "/" + fileKey + "?uuid=" + uuid);
                video.setDuration(duration);
            } finally {
                LocalCache.rem(uuid);
            }
        }

        this.saveOrUpdate(video);

        final VideoTask videoTask = new VideoTask();
        videoTask.setOldVideo(video);
        videoTask.setVideo(video);
        videoTask.setIsAdd(isAdd);
        videoTask.setOldState(isAdd ? true : video.getOpen());
        videoTask.setNewState(true);
        videoPublishAuditService.audit(videoTask, false);
    }

    @Override
    @Async
    public void historyVideo(Long videoId, Long userId) {
        String key = RedisConstant.HISTORY_VIDEO + videoId + ":" + userId;
        final Object o = redisCacheUtil.get(key);
        if (o == null) {
            redisCacheUtil.set(key, videoId, RedisConstant.HISTORY_TIME);
            final Video video = getById(videoId);
            video.setUser(userService.getInfo(video.getUserId()));
            video.setTypeName(typeService.getById(video.getTypeId()).getName());
            redisCacheUtil.zadd(RedisConstant.USER_HISTORY_VIDEO + userId, new Date().getTime(), video, RedisConstant.HISTORY_TIME);
            updateHistory(video, 1L);
        }
    }

    @Override
    public LinkedHashMap<String, List<Video>> getHistory(BasePage basePage) {

        final Long userId = UserHolder.get();
        String key = RedisConstant.USER_HISTORY_VIDEO + userId;
        final Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.zSetGetByPage(key, basePage.getPage(), basePage.getLimit());
        if (ObjectUtils.isEmpty(typedTuples)) {
            return new LinkedHashMap<>();
        }
        List<Video> temp = new ArrayList<>();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final LinkedHashMap<String, List<Video>> result = new LinkedHashMap<>();
        for (ZSetOperations.TypedTuple<Object> typedTuple : typedTuples) {
            final Date date = new Date(typedTuple.getScore().longValue());
            final String format = simpleDateFormat.format(date);
            if (!result.containsKey(format)) {
                result.put(format, new ArrayList<>());
            }
            final Video video = (Video) typedTuple.getValue();
            result.get(format).add(video);
            temp.add(video);
        }
        setUserVoAndUrl(temp);

        return result;
    }

    @Override
    public void deleteVideo(Long id) {
        if (id == null) {
            throw new BaseException("删除指定的视频不存在");
        }

        final Long userId = UserHolder.get();
        final Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getId, id).eq(Video::getUserId, userId));
        if (video == null) {
            throw new BaseException("删除指定的视频不存在");
        }
        final boolean b = removeById(id);
        if (b) {
            // 解耦
            new Thread(() -> {
                // 删除分享量 点赞量
                videoShareService.remove(new LambdaQueryWrapper<VideoShare>().eq(VideoShare::getVideoId, id).eq(VideoShare::getUserId, userId));
                videoStarService.remove(new LambdaQueryWrapper<VideoStar>().eq(VideoStar::getVideoId, id).eq(VideoStar::getUserId, userId));
                interestPushService.deleteSystemStockIn(video);
                interestPushService.deleteSystemTypeStockIn(video);
            }).start();
        }
    }

    @Override
    public void auditProcess(Video video) {
        // 放行后
        updateById(video);
        interestPushService.pushSystemStockIn(video);
        interestPushService.pushSystemTypeStockIn(video);
        // 推送该视频博主的发件箱
        feedService.pushInBoxFeed(video.getUserId(), video.getId(), video.getGmtCreated().getTime());

    }

    @Override
    public void violations(Long id) {
        final Video video = getById(id);
        final Type type = typeService.getById(video.getTypeId());
        video.setLabelNames(type.getLabelNames());
        // 修改视频信息
        video.setOpen(true);
        video.setMsg("该视频违反了幸运日平台的规则,已被下架私密");
        video.setAuditStatus(AuditStatus.PASS);
        // 删除分类中的视频
        interestPushService.deleteSystemTypeStockIn(video);
        // 删除标签中的视频
        interestPushService.deleteSystemStockIn(video);
        // 获取视频发布者id,删除对应的发件箱
        final Long userId = video.getUserId();
        redisTemplate.opsForZSet().remove(RedisConstant.OUT_FOLLOW + userId, id);

        // 获取视频发布者粉丝，删除对应的收件箱
        final Collection<Long> fansIds = followService.getFans(userId, null);
        feedService.deleteInBoxFeed(userId, Collections.singletonList(id));
        feedService.deleteOutBoxFeed(userId, fansIds, id);

        // 热门视频以及热度排行榜视频
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DATE);
        final Long videoId = video.getId();
        // 尝试去找到删除
        redisTemplate.opsForSet().remove(RedisConstant.HOT_VIDEO + today, videoId);
        redisTemplate.opsForSet().remove(RedisConstant.HOT_VIDEO + (today - 1), videoId);
        redisTemplate.opsForSet().remove(RedisConstant.HOT_VIDEO + (today - 2), videoId);
        redisTemplate.opsForZSet().remove(RedisConstant.HOT_RANK, videoId);
        // 修改视频
        updateById(video);
    }

    /**
     * 浏览量
     *
     * @param video
     */
    public void updateHistory(Video video, Long value) {
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("history_count = history_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId());
        update(video, updateWrapper);
    }
    /**
     * 点赞数
     *
     * @param video
     */
    public void updateStar(Video video, Long value) {
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("start_count = start_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getStartCount, video.getStartCount());
        update(video, updateWrapper);
    }

    public void updateFavorites(Video video, Long value) {
        final UpdateWrapper<Video> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("favorites_count = favorites_count + " + value);
        updateWrapper.lambda().eq(Video::getId, video.getId()).eq(Video::getFavoritesCount, video.getFavoritesCount());
        update(video, updateWrapper);
    }

    public void setUserVoAndUrl(Collection<Video> videos) {
        if (!ObjectUtils.isEmpty(videos)) {
            Set<Long> userIds = new HashSet<>();
            final ArrayList<Long> fileIds = new ArrayList<>();
            for (Video video : videos) {
                userIds.add(video.getUserId());
                fileIds.add(video.getUrl());
                fileIds.add(video.getCover());
            }
            final Map<Long, File> fileMap = fileService.listByIds(fileIds).stream().collect(Collectors.toMap(File::getId, Function.identity()));
            final Map<Long, User> userMap = userService.list(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));
            for (Video video : videos) {
                final UserVO userVO = new UserVO();
                final User user = userMap.get(video.getUserId());
                userVO.setId(video.getUserId());
                userVO.setNickName(user.getNickName());
                userVO.setDescription(user.getDescription());
                userVO.setSex(user.getSex());
                video.setUser(userVO);
                final File file = fileMap.get(video.getUrl());
                video.setVideoType(file.getFormat());
            }
        }

    }
}
