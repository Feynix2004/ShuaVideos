package org.shuavideos.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.shuavideos.entity.user.Favorites;
import org.shuavideos.entity.vo.BasePage;
import org.shuavideos.entity.vo.UpdateUserVO;
import org.shuavideos.holder.UserHolder;
import org.shuavideos.service.user.FavoritesService;
import org.shuavideos.service.user.UserService;
import org.shuavideos.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/luckyjourney/customer")
public class CustomerController {



    @Autowired
    private FavoritesService favoritesService;

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

    /**
     * 添加/修改收藏夹
     * @param favorites
     * @return
     */
    @PostMapping("/favorites")
    public R saveOrUpdateFavorites(@RequestBody @Validated Favorites favorites){
        final Long userId = UserHolder.get();
        final Long id = favorites.getId();
        favorites.setUserId(userId);
        try{
            favoritesService.saveOrUpdate(favorites);
        }catch (Exception e){
            return R.error().message("已存在相同名称的收藏夹");
        }

        return R.ok().message(id !=null ? "修改成功" : "添加成功");
    }

    /**
     * 获取所有的收藏夹
     * @return
     */
    @GetMapping("/favorites")
    public R listFavorites(){
        final Long userId = UserHolder.get();
        List<Favorites> favorites = favoritesService.listByUserId(userId);
        return R.ok().data(favorites);
    }

    /**
     * 获取指定收藏夹
     * @param id
     * @return
     */
    @GetMapping("/favorites/{id}")
    public R getFavorites(@PathVariable Long id){
        final Long userId = UserHolder.get();

        return R.ok().data(favoritesService.getOne(new LambdaQueryWrapper<Favorites>().eq(Favorites::getUserId, userId).eq(Favorites::getId, id)));
    }



}
