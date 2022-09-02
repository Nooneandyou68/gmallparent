package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.stereotype.Component;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/1 18:52
 */
@Component
public class ItemDegradeFeignClient implements ItemFeignClient{
    @Override
    public Result getItem(Long skuId) {
        return null;
    }
}
