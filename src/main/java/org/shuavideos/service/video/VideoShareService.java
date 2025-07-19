package org.shuavideos.service.video;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shuavideos.entity.video.VideoShare;

import java.util.List;

public interface VideoShareService extends IService<VideoShare> {
    /**
     * 添加分享记录
     * @param videoShare
     */
    boolean share(VideoShare videoShare);



    /**
     * 获取分享用户id
     * @param videoId
     * @return
     */
    List<Long> getShareUserId(Long videoId);
}
