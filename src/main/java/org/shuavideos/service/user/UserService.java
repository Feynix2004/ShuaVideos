package org.shuavideos.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import org.shuavideos.entity.user.User;
import org.shuavideos.entity.vo.*;

import java.util.Collection;
import java.util.List;

public interface UserService extends IService<User> {


    /**
     * 获取用户信息:
     * 1.用户基本信息
     * 2.关注数量
     * 3.粉丝数量
     * @param userId 用户id
     * @return
     */
    UserVO getInfo(Long userId);
    Boolean register(RegisterVO registerVO);

    Boolean findPassword(FindPWVO findPWVO);

    /**
     * 获取用户基本信息
     * @param userIds
     * @return
     */
    List<User> list(Collection<Long> userIds);

    /**
     * 修改用户资料
     * @param user
     */
    void updateUser(UpdateUserVO user);

    /**
     * 关注/取关
     *
     * @param followsUserId
     * @param userId
     * @return
     */
    boolean follows(Long followsUserId, Long userId);

    /**
     * 获取关注
     * @param userId
     * @param basePage
     * @return
     */
    Page<User> getFollows(Long userId, BasePage basePage);

    /**
     * 获取粉丝
     * @param userId
     * @param basePage
     * @return
     */
    Page<User> getFans(Long userId, BasePage basePage);
}
