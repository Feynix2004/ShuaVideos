package org.shuavideos.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shuavideos.constant.RedisConstant;
import org.shuavideos.service.FeedService;
import org.shuavideos.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class FeedServiceImpl implements FeedService {


    @Autowired
    public RedisTemplate redisTemplate;


    @Override
    @Async
    public void updateFollowFeed(Long userId, Collection<Long> followIds) {
        String t2 = RedisConstant.IN_FOLLOW;
        final Date curDate = new Date();
        final Date limitDate = DateUtil.addDateDays(curDate, -7);

        final Set<ZSetOperations.TypedTuple<Long>> set = redisTemplate.opsForZSet().rangeWithScores(t2 + userId, 0, -1);
        if (!ObjectUtils.isEmpty(set)) {
            Double oldTime = set.iterator().next().getScore();
            update(userId,oldTime.longValue(),new Date().getTime(),followIds);
        } else {
            update(userId,limitDate.getTime(),curDate.getTime(),followIds);
        }
    }

    public void update(Long userId, Long min, Long max, Collection<Long> followIds) {
        String t1 = RedisConstant.OUT_FOLLOW;
        String t2 = RedisConstant.IN_FOLLOW;
        // 查看关注人的发件箱
        final List<Set<DefaultTypedTuple>> result = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Long followId : followIds) {
                connection.zRevRangeByScoreWithScores((t1 + followId).getBytes(), min, max, 0, 50);
            }
            return null;
        });
        final ObjectMapper objectMapper = new ObjectMapper();
        final HashSet<Long> ids = new HashSet<>();
        // 放入收件箱
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (Set<DefaultTypedTuple> tuples : result) {
                if (!ObjectUtils.isEmpty(tuples)) {

                    for (DefaultTypedTuple tuple : tuples) {

                        final Object value = tuple.getValue();
                        ids.add(Long.parseLong(value.toString()));
                        final byte[] key = (t2 + userId).getBytes();
                        try {
                            connection.zAdd(key, tuple.getScore(), objectMapper.writeValueAsBytes(value));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                        connection.expire(key, RedisConstant.HISTORY_TIME);
                    }
                }
            }
            return null;
        });
    }
}
