package org.shuavideos.limit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.shuavideos.constant.RedisConstant;
import org.shuavideos.exception.LimiterException;
import org.shuavideos.holder.UserHolder;
import org.shuavideos.util.RedisCacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

@Aspect
public class LimiterAop {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    /**
     * 拦截
     * @param joinPoint
     * @param limiter
     * @return
     * @throws Throwable
     */
    @Before("@annotation(limiter)")
    public Object restriction(ProceedingJoinPoint joinPoint, Limit limiter) throws Throwable {
        final Long userId = UserHolder.get();
        final int limitCount = limiter.limit();
        final String msg = limiter.msg();
        final long time = limiter.time();
        // 缓存是否存在
        String key = RedisConstant.VIDEO_LIMIT + userId;
        final Object o1 = redisCacheUtil.get(key);
        if (ObjectUtils.isEmpty(o1)){
            redisCacheUtil.set(key,1,time);
        }else {
            if (Integer.parseInt(o1.toString()) > limitCount){
                throw new LimiterException(msg);
            }
            redisCacheUtil.incr(key,1);
        }
        Object o = joinPoint.proceed();
        return o;
    }

}
