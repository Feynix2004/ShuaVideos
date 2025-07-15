package org.shuavideos.controller;


import org.shuavideos.entity.video.Video;
import org.shuavideos.entity.vo.BasePage;
import org.shuavideos.service.video.VideoService;
import org.shuavideos.util.JwtUtils;
import org.shuavideos.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/luckyjourney/index")
public class IndexController {



    @Autowired
    private VideoService videoService;

    /**
     * 根据用户id获取视频
     * @param userId
     * @param basePage
     * @return
     */
    @GetMapping("/video/user")
    public R listVideoByUserId(@RequestParam(required = false) Long userId,
                               BasePage basePage, HttpServletRequest request){

        userId = userId == null ? JwtUtils.getUserId(request) : userId;
        return R.ok().data(videoService.listByUserIdOpenVideo(userId,basePage));
    }

    /**
     * 根据视频分类获取
     * @param typeId
     * @return
     */
    @GetMapping("/video/type/{typeId}")
    public R getVideoByTypeId(@PathVariable Long typeId){

        return R.ok().data(videoService.getVideoByTypeId(typeId));
    }

    /**
     * 获取热度排行榜
     * @return
     */
    @GetMapping("/video/hot/rank")
    public R listHotRank(){
        return R.ok().data(videoService.hotRank());
    }

    /**
     * 推送热门视频
     * @return
     */
    @GetMapping("/video/hot")
    public R listHotVideo(){
        return R.ok().data(videoService.listHotVideo());
    }

    /**
     * 根据视频标签推送相似视频
     * @param video
     * @return
     */
    @GetMapping("/video/similar")
    public R pushVideoSimilar(Video video){
        return R.ok().data(videoService.listSimilarVideo(video));
    }

}
