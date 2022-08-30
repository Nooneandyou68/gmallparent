package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SkuInfo;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/30 20:17
 */
public interface SkuInfoService {

    /**
     * 修改sku数据回显
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/30 20:13
     */
    SkuInfo getSkuInfo(Long id);

    /**
     * 保存修改sku
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/30 23:48
     */
    void updateSkuInfo(SkuInfo skuInfo);

}
