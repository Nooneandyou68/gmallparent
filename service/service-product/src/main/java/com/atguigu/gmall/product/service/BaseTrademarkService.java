package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/27 18:39
 */
public interface BaseTrademarkService {

    /**
     * 品牌分页列表
     *
     * @param
     * @return
     */
    IPage<BaseTrademark> getBaseTrademarkList(Page<BaseTrademark> trademarkPage);

}
