package org.shuavideos.controller;


import org.shuavideos.entity.vo.BasePage;
import org.shuavideos.service.video.VideoService;
import org.shuavideos.util.JwtUtils;
import org.shuavideos.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
