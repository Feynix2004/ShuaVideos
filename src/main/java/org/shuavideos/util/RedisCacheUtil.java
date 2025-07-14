package org.shuavideos.util;


import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    /**
     * HashSet
     *
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hmset(String key, Map<Object, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public Object sRandom(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    public List<Object> sRandom(List<String> keys){
        final List<Object> list = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {

                for (String key : keys) {
                    connection.sRandMember(key.getBytes());
                }
                return null;
            }
        });
        // 可能会有null
        final List result = new ArrayList();
        for (Object aLong : list) {
            if (aLong!=null){
                result.add(aLong);
            }
        }
        return result;
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {

        return redisTemplate.opsForHash().entries(key);

    }
}
