package com.atguigu.gmall.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/4 16:12
 */
@Configuration
public class ThreadPoolExecutorConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
                7,//核心线程数
                12,//最大线程数
                3,//存活时间
                TimeUnit.SECONDS,//时间单位
                new ArrayBlockingQueue<>(3),//阻塞队列
                Executors.defaultThreadFactory(),//线程工厂
                new ThreadPoolExecutor.AbortPolicy()//拒绝策略
        );
    }
}
