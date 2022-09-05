package com.atguigu.gmall.common.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/3 20:05
 */
@Target({ElementType.METHOD}) //  这个注解使用范围
@Retention(RetentionPolicy.RUNTIME) //  表示注解的声明周期
public @interface GmallCache {

    //  在这里定义一个属性： 组成缓存key 的前缀！
    //  key = categoryView:category3Id 如果知道参数一样了，前缀不能一样！
    String prefix() default "cache";
}
