package org.shuavideos.util;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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

}
