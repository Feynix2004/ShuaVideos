package org.shuavideos.controller;

import org.shuavideos.entity.vo.BasePage;
import org.shuavideos.entity.vo.UpdateUserVO;
import org.shuavideos.holder.UserHolder;
import org.shuavideos.service.user.UserService;
import org.shuavideos.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/luckyjourney/customer")
public class CustomerController {
    @Autowired
    private UserService userService;
    /**
     * 获取个人信息
     * @param userId
     * @return
     * @throws Exception
     */
    @GetMapping("/getInfo/{userId}")
    public R getInfo(@PathVariable Long userId){
        return R.ok().data(userService.getInfo(userId));
    }

    /**
     * 获取用户信息
     * @return
     */
    @GetMapping("/getInfo")
    public R getDefaultInfo(){
        return R.ok().data(userService.getInfo(UserHolder.get()));
    }

    /**
     *  修改用户信息
     * @param user
     * @return
     */
    @PutMapping
    public R updateUser(@RequestBody @Validated UpdateUserVO user){
        user.setUserId(UserHolder.get());
        userService.updateUser(user);
        return R.ok().message("修改成功");
    }

    /**
     * 获取关注人员
     * @param basePage
     * @param userId
     * @return
     */
    @GetMapping("/follows")
    public R getFollows(BasePage basePage, Long userId){
        return R.ok().data(userService.getFollows(userId,basePage));
    }
    /**
     * 获取粉丝
     * @param basePage
     * @param userId
     * @return
     */
    @GetMapping("/fans")
    public R getFans(BasePage basePage,Long userId){
        return R.ok().data(userService.getFans(userId,basePage));
    }

    @PostMapping("/follows")
    public R follows(@RequestParam Long followsUserId){
        final Long userId = UserHolder.get();
        return R.ok().message(userService.follows(followsUserId, userId) ? "已关注" : "已取关");
    }
}
