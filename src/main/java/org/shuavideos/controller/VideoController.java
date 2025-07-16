package org.shuavideos.controller;

import org.shuavideos.entity.video.Video;
import org.shuavideos.holder.UserHolder;
import org.shuavideos.limit.Limit;
import org.shuavideos.service.video.VideoService;
import org.shuavideos.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;


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


    /**
     * 更新收件箱
     * @return
     */
    @PostMapping("/init/follow/feed")
    public R updateFollowFeed(){
        final Long userId = UserHolder.get();
        videoService.updateFollowFeed(userId);
        return R.ok();
    }


    /**
     * 推送关注的人视频 拉模式
     * @param lastTime 滚动分页
     * @return
     */
    @GetMapping("/follow/feed")
    public R followFeed(@RequestParam(required = false) Long lastTime) throws ParseException {
        final Long userId = UserHolder.get();

        return R.ok().data(videoService.followFeed(userId,lastTime));
    }

    /**发布视频/修改视频
     * @param video
     * @return
     */
    @PostMapping
    @Limit(limit = 5,time = 3600L,msg = "发布视频一小时内不可超过5次")
    public R publishVideo(@RequestBody @Validated Video video){
        videoService.publishVideo(video);
        return R.ok().message("发布成功,请等待审核");
    }
}
