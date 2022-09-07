package com.atguigu.gmall.list.service;

import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

import java.io.IOException;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/6 18:38
 */

public interface SearchService {

    /**
     * 上架商品列表
     * @param skuId
     */
    void upperGoods(Long skuId);

    /**
     * 下架商品列表
     * @param skuId
     */
    void lowerGoods(Long skuId);

    /**
     * 更新热点
     * @param skuId
     */
    void incrHotScore(Long skuId);

    /**
     * 检索
     *
     * @param searchParam
     */
    SearchResponseVo search(SearchParam searchParam) throws IOException;

}
