package org.shuavideos.controller;

import org.shuavideos.holder.UserHolder;
import org.shuavideos.service.video.VideoService;
import org.shuavideos.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/luckyjourney/video")
public class VideoController {

    @Autowired
    private VideoService videoService;
    /**
     * 收藏视频
     * @param fId
     * @param vId
     * @return
     */
    @PostMapping("/favorites/{fId}/{vId}")
    public R favoritesVideo(@PathVariable Long fId, @PathVariable Long vId){
        final Long uId = UserHolder.get();
        String msg = videoService.favoritesVideo(fId,vId, uId) ? "已收藏" : "取消收藏";
        return R.ok().message(msg);
    }
}
