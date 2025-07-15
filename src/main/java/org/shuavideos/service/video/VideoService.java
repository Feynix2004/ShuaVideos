package org.shuavideos.service.video;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.shuavideos.entity.video.Video;
import org.shuavideos.entity.vo.BasePage;
import org.shuavideos.entity.vo.HotVideo;

import java.util.Collection;
import java.util.List;

public interface VideoService extends IService<Video> {
    /**
     * 根据userId获取对应视频,只包含公开的
     * @param userId
     * @return
     */
    IPage<Video> listByUserIdOpenVideo(Long userId, BasePage basePage);

    /**
     * 收藏视频
     *
     * @param fId
     * @param vId
     * @param uId
     * @return
     */
    boolean favoritesVideo(Long fId, Long vId, Long uId);

    /**
     * 视频点赞
     * @param videoId
     * @return
     */
    boolean startVideo(Long videoId);

    /**
     * 根据视频分类获取视频,乱序
     * @param typeId
     * @return
     */
    Collection<Video> getVideoByTypeId(Long typeId);

    /**
     * 获取N天前的视频
     * @param id id
     * @param days 天数
     * @param limit 限制
     * @return
     */
    List<Video> selectNDaysAgeVideo(long id, int days, int limit);

    /**
     * 获取热度排行榜
     * @return
     */
    Collection<HotVideo> hotRank();

    /**
     * 获取热门视频
     * @return
     */
    Collection<Video> listHotVideo();

    /**
     * 根据标签推送相似视频
     * @param video
     * @return
     */
    Collection<Video> listSimilarVideo(Video video);



    /**
     * 拉模式
     * @param userId
     */
    void updateFollowFeed(Long userId);

    /**
     * 关注流
     * @param userId 用户id
     * @param lastTime 滚动分页参数，首次为null，后续为上次的末尾视频时间
     * @return
     */
    Collection<Video> followFeed(Long userId,Long lastTime);
}
