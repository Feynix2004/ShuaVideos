package org.shuavideos.service.video.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shuavideos.constant.AuditStatus;
import org.shuavideos.entity.File;
import org.shuavideos.entity.user.User;
import org.shuavideos.entity.video.Video;
import org.shuavideos.entity.vo.BasePage;
import org.shuavideos.entity.vo.UserModel;
import org.shuavideos.entity.vo.UserVO;
import org.shuavideos.exception.BaseException;
import org.shuavideos.mapper.video.VideoMapper;
import org.shuavideos.service.FileService;
import org.shuavideos.service.user.FavoritesService;
import org.shuavideos.service.user.UserService;
import org.shuavideos.service.video.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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
    private UserService userService;

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
    public boolean favoritesVideo(Long fId, Long vId, Long uId) {
        final Video video = getById(vId);
        if (video == null) {
            throw new BaseException("指定视频不存在");
        }
        final boolean favorites = favoritesService.favorites(fId, vId, uId);
        updateFavorites(video, favorites ? 1L : -1L);

        final List<String> labels = video.buildLabel();
        // 根据收藏行为动态计算权重
        double weight = favorites ? 2.0 : -2.0;
        final UserModel userModel = UserModel.buildUserModel(labels, vId, weight);
      // TODO 用户兴趣模型修改
        //  interestPushService.updateUserModel(userModel);

        return favorites;
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
