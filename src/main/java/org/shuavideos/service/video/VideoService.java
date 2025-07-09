package org.shuavideos.service.video;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.shuavideos.entity.video.Video;
import org.shuavideos.entity.vo.BasePage;

public interface VideoService extends IService<Video> {
    /**
     * 根据userId获取对应视频,只包含公开的
     * @param userId
     * @return
     */
    IPage<Video> listByUserIdOpenVideo(Long userId, BasePage basePage);
}
