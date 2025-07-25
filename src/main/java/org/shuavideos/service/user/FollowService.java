package org.shuavideos.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shuavideos.entity.user.Follow;
import org.shuavideos.entity.vo.BasePage;

import java.util.Collection;

public interface FollowService extends IService<Follow> {
    /**
     * 获取关注数量
     * @param userId
     * @return
     */
    int getFollowCount(Long userId);

    /**
     * 获取粉丝数量
     * @param userId
     * @return
     */
    int getFansCount(Long userId);

    /**
     * 关注/取关
     * @param followId 对方id
     * @param userId 自己id
     * @return
     */
    Boolean follows(Long followId,Long userId);


    /**
     * 获取关注人员且按照关注时间排序
     * @param userId
     * @return
     */
    Collection<Long> getFollow(Long userId, BasePage basePage);


    /**
     * 获取粉丝人员且安排关注时间排序
     * @param userId
     * @return
     */
    Collection<Long> getFans(Long userId, BasePage basePage);

    Boolean isFollows(Long followId, Long userId);
}
