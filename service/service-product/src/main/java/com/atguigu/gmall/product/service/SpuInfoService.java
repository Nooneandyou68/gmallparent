package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.SpuInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/27 18:16
 */
public interface SpuInfoService {
    /**
     * 根据三级分类Id 查询spu 列表！
     */
    IPage<SpuInfo> getSpuList(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo);

    /**
     * 保存spu数据
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);
}
