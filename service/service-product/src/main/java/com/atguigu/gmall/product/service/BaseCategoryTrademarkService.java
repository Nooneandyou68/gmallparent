package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/28 16:49
 */
public interface BaseCategoryTrademarkService extends IService<BaseCategoryTrademark> {
    /**
     * 查询分类品牌列表
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 16:56
     */
    List<BaseTrademark> findTrademarkList(Long category3Id);

    /**
     * 删除品牌关联
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 23:11
     */
    void removeBaseCategoryTrademarkById(Long category3Id, Long trademarkId);

    /**
     * 获取可选品牌列表
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 23:04
     */
    List<BaseTrademark> findCurrentTrademarkList(Long category3Id);

    /**
     * 保存分类品牌关联
     *
     * @param categoryTrademarkVo
     * @return void
     * @author SongBoHao
     * @date 2022/8/29 16:29
     */
    void save(CategoryTrademarkVo categoryTrademarkVo);
}
