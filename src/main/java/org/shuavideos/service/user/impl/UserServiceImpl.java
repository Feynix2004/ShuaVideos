package org.shuavideos.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shuavideos.constant.RedisConstant;
import org.shuavideos.entity.user.Favorites;
import org.shuavideos.entity.user.User;
import org.shuavideos.entity.vo.FindPWVO;
import org.shuavideos.entity.vo.RegisterVO;
import org.shuavideos.exception.BaseException;
import org.shuavideos.mapper.user.UserMapper;
import org.shuavideos.service.user.FavoritesService;
import org.shuavideos.service.user.UserService;
import org.shuavideos.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private FavoritesService favoritesService;
    @Override
    public Boolean register(RegisterVO registerVO){
        //判断邮箱是否存在
        final int count = count(new LambdaQueryWrapper<User>().eq(User::getEmail, registerVO.getEmail()));
        if(count == 1){
            throw new BaseException("邮箱已注册");
        }
        final String code = registerVO.getCode();
        final Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE+registerVO.getEmail());
        if(o==null) throw new BaseException("验证码为空");
        if(!code.equals(o)) return false;
        final User user = new User();
        user.setNickName(registerVO.getNickName());
        user.setEmail(registerVO.getEmail());
        user.setDescription("这个人很懒。。。");
        user.setPassword(registerVO.getPassword());
        save(user);

        //创建默认收藏夹
        final Favorites favorites = new Favorites();
        favorites.setUserId(user.getId());
        favorites.setName("默认收藏夹");
        favoritesService.save(favorites);

        user.setDefaultFavoritesId(favorites.getId());
        updateById(user);
        return true;

    }

    @Override
    public Boolean findPassword(FindPWVO findPWVO) {
        //从redis中取出
        final Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE+findPWVO.getEmail());
        if(o==null) return false;
        //校验
        if(Integer.parseInt(o.toString())!=findPWVO.getCode()) return false;

        //修改
        final User user = new User();
        user.setEmail(findPWVO.getEmail());
        user.setPassword(findPWVO.getNewPassword());
        update(user, new UpdateWrapper<User>().lambda().set(User::getPassword, findPWVO.getNewPassword()).eq(User::getEmail, findPWVO.getEmail()));

        return true;
    }
}
