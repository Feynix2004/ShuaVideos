package org.shuavideos.service.video;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shuavideos.entity.video.VideoStar;

public interface VideoStarService extends IService<VideoStar> {
    /**
     * 视频点赞
     * @param videoStar
     */
    boolean starVideo(VideoStar videoStar);

    Boolean starState(Long videoId, Long userId);
}
