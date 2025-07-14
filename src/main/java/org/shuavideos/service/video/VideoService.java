package org.shuavideos.service.video;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.shuavideos.entity.video.Video;
import org.shuavideos.entity.vo.BasePage;

import java.util.Collection;

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
}
