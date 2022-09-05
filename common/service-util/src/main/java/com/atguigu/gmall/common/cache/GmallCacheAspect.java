package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/3 20:12
 */
@Component
@Aspect//标识一个切面类
public class GmallCacheAspect {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 切面方法
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/9/3 20:19
     */
    @SneakyThrows
    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object setGmallCacheAspect(ProceedingJoinPoint joinPoint) {
        Object object = null;
            //1、获取缓存key 由注解的前缀：方法的参数组成
            //1.1获取注解
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
            String prefix = gmallCache.prefix();
            //组成key
        Object[] args = joinPoint.getArgs();
            String key = prefix + Arrays.asList(args).toString();
            try {//2、通过key获取缓存中的数据 true：返回数据 false 上锁 查询数据放入缓存 解锁
            //Object o = redisTemplate.opsForValue().get(key); 封装到一个方法中
            object = this.getRedisData(key, signature);
            if (object == null) {
                //锁的key
                String locKey = key + ":lock";
                RLock lock = this.redissonClient.getLock(locKey);
                //上锁
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                //判断是否拿到锁
                if (res) {
                    //查询数据库,proceed：主席那个原有方法，执行方法体
                    try {
                        object = joinPoint.proceed(args);
                        if (object == null) {
                            Object o = new Object();
                            redisTemplate.opsForValue().set(key, JSON.toJSONString(o), RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return o;
                        }
                        //如果有数据，放入缓存
                        redisTemplate.opsForValue().set(key, JSON.toJSONString(object), RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        return object;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    } finally {
                        //解锁
                        lock.unlock();
                    }
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return setGmallCacheAspect(joinPoint);
                }
            } else {
                return object;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return joinPoint.proceed(args);
    }

    /**
     * 获取缓存数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/9/3 20:59
     */
    private Object getRedisData(String key, MethodSignature methodSignature) {
        String strJson = (String) this.redisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(strJson)) {
            //将这个字符串转换为具体的数据类型
            return JSON.parseObject(strJson, methodSignature.getReturnType());
        }
        //默认返回
        return null;
    }

}
