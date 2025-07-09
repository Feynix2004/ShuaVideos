package org.shuavideos.util;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheUtil {

    private RedisTemplate<String, Object> redisTemplate;




    public Object get(String key){return key==null? null : redisTemplate.opsForValue().get(key);}

    public boolean set(String key, Object value){
        try{
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e){
            return false;
        }
    }
    public boolean set(String key, Object value, long time){
        try{
            if(time>0){
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public Set zGet(String key){

        return redisTemplate.opsForZSet().reverseRange(key,0,-1);
    }

    public Set<ZSetOperations.TypedTuple<Object>> zSetGetByPage(String key, long pageNum, long pageSize) {
        try {
            if (redisTemplate.hasKey(key)) {
                long start = (pageNum - 1) * pageSize;
                long end = pageNum * pageSize - 1;
                Long size = redisTemplate.opsForZSet().size(key);
                if (end > size) {
                    end = -1;
                }

                return redisTemplate.opsForZSet().reverseRangeWithScores(key,start,end);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
