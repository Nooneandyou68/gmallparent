package com.atguigu.gmall.item.service;

import java.util.Map;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/31 16:21
 */
public interface ItemService {
    /**
     * 获取sku详情信息
     *
     * @param skuId
     * @return
     */
    Map<String, Object> getBySkuId(Long skuId);
}
