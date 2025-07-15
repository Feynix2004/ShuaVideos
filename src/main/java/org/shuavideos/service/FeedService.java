package org.shuavideos.service;

import java.util.Collection;

public interface FeedService {





    /**
     * 初始化关注流-拉模式 with TTL
     * @param userId
     */
    void updateFollowFeed(Long userId, Collection<Long> followIds);
}
