package com.qingcheng.Utils;

import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

/*针对操作缓存数据库做的模板
    1. 先操作缓存
    2. 缓存失败，操作数据库并且设置缓存
    3. 再次读取缓存
    4. 不使用缓存，直接操作数据库
*/
public interface ICacheHandler<T> {


    /*拿到缓存对象*/
    RedisTemplate getredis();

    /*第一次操作缓存*/
    T firstFromCache();

    /*尝试拿到操作次数*/
    int getTryTimes();

    /*直接操作数据库*/
    T directNoCache();
   /* 拿到超时时间*/
    long getExpireTime();

    /*第一次操作数据库失败，第二次操作数据库*/
    void secondFromDbAndSetCache();

    /*拿到线程睡眠的时间*/
    long getCacheTryWait();

    default T handlerPrivate(int retryTimes){
        try {
            return firstFromCache();
        }catch (Exception e){
            if (retryTimes>=getTryTimes()) return directNoCache();
            String cacheKey = "xxxxx"; //拿到缓存的key
            Boolean success = getredis().opsForValue().setIfAbsent(cacheKey, 1);
            if (!success.booleanValue()){
                    try {
                        Thread.sleep(getCacheTryWait());
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    return handlerPrivate(getTryTimes());
            }
            try {
                getredis().opsForValue().set(cacheKey,1,getExpireTime(), TimeUnit.SECONDS);
                secondFromDbAndSetCache();
            }finally {
                // 释放锁
            }
            return handlerPrivate(getTryTimes());
        }
    }
}


