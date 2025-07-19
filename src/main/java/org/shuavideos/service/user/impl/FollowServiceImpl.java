package org.shuavideos.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shuavideos.constant.RedisConstant;
import org.shuavideos.entity.user.Follow;
import org.shuavideos.entity.vo.BasePage;
import org.shuavideos.exception.BaseException;
import org.shuavideos.mapper.user.FollowMapper;
import org.shuavideos.service.user.FollowService;
import org.shuavideos.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public int getFollowCount(Long userId) {
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId));
    }

    @Override
    public int getFansCount(Long userId) {
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, userId));
    }

    @Override
    public Boolean follows(Long followsId, Long userId) {
        if (followsId.equals(userId)) {
            throw new BaseException("你不能关注自己");
        }

        // 直接保存(唯一索引),保存失败则删除
        final Follow follow = new Follow();
        follow.setFollowId(followsId);
        follow.setUserId(userId);
        try {
            save(follow);
            final Date date = new Date();
            // 自己关注列表添加
            redisTemplate.opsForZSet().add(RedisConstant.USER_FOLLOW + userId, followsId, date.getTime());
            // 对方粉丝列表添加
            redisTemplate.opsForZSet().add(RedisConstant.USER_FANS + followsId, userId, date.getTime());
        } catch (Exception e) {
            remove(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, followsId).eq(Follow::getUserId, userId));

            // TODO 删除视频
//            // 删除收件箱的视频
//            // 获取关注人的视频
//            final List<Long> videoIds = (List<Long>) videoService.listVideoIdByUserId(followsId);
//            feedService.deleteInBoxFeed(userId, videoIds);


            // 自己关注列表删除
            redisTemplate.opsForZSet().remove(RedisConstant.USER_FOLLOW + userId, followsId);
            // 对方粉丝列表删除
            redisTemplate.opsForZSet().remove(RedisConstant.USER_FANS + followsId, userId);

            return false;
        }

        return true;
    }

    @Override
    public Collection<Long> getFollow(Long userId, BasePage basePage) {
        if (basePage == null) {
            final Set<Object> set = redisCacheUtil.zGet(RedisConstant.USER_FANS + userId);
            if(ObjectUtils.isEmpty(set)){
                return Collections.EMPTY_SET;
            }
            return set.stream().map(o->Long.valueOf(o.toString())).collect(Collectors.toList());
        }
        final Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.zSetGetByPage(RedisConstant.USER_FANS + userId, basePage.getPage(), basePage.getLimit());
        if (ObjectUtils.isEmpty(typedTuples)) {
            final List<Follow> follows = page(basePage.page(),new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, userId)).getRecords();
            if (ObjectUtils.isEmpty(follows)){
                return Collections.EMPTY_LIST;
            }
            return follows.stream().map(Follow::getUserId).collect(Collectors.toList());
        }
        return typedTuples.stream().map(t -> Long.parseLong(t.getValue().toString())).collect(Collectors.toList());
    }

    @Override
    public Collection<Long> getFans(Long userId, BasePage basePage) {
        if (basePage == null) {
            final Set<Object> set = redisCacheUtil.zGet(RedisConstant.USER_FANS + userId);
            if(ObjectUtils.isEmpty(set)){
                return Collections.EMPTY_SET;
            }
            return set.stream().map(o->Long.valueOf(o.toString())).collect(Collectors.toList());
        }
        final Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisCacheUtil.zSetGetByPage(RedisConstant.USER_FANS + userId, basePage.getPage(), basePage.getLimit());
        if (ObjectUtils.isEmpty(typedTuples)) {
            final List<Follow> follows = page(basePage.page(),new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, userId)).getRecords();
            if (ObjectUtils.isEmpty(follows)){
                return Collections.EMPTY_LIST;
            }
            return follows.stream().map(Follow::getUserId).collect(Collectors.toList());
        }
        return typedTuples.stream().map(t -> Long.parseLong(t.getValue().toString())).collect(Collectors.toList());
    }

    @Override
    public Boolean isFollows(Long followId, Long userId) {
        if (userId == null || followId == null) return false;
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId,followId).eq(Follow::getUserId,userId)) == 1;
    }
}
