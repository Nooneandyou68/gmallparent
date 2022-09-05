package com.atguigu.gmall.product;

import com.atguigu.gmall.common.constant.RedisConst;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @PROJECT_NAME: gamallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/26 10:12
 */
@SpringBootApplication
@ComponentScan({"com.atguigu.gmall"})
@EnableDiscoveryClient
public class ServiceProductApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(ServiceProductApplication.class, args);
    }

    @Autowired
    private RedissonClient redissonClient;

    //初始化方法
    @Override
    public void run(String... args) throws Exception {
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        //设置好数据规模和数据误判率
        bloomFilter.tryInit(10000, 0.0001);
    }
}
