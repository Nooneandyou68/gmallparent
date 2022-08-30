package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * @PROJECT_NAME: gamallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/26 10:22
 */
public interface ManageService {
    /**
     * 查询所有
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 10:27
     */
    List<BaseCategory1> getCategory1();

    /**
     * 根据一级id查询二级分类数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 10:38
     */
    List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * 根据二级id查询三级分类数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 10:36
     */
    List<BaseCategory3> getCategory3(Long category2Id);

    /**
     * 根据分类Id 获取平台属性数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 18:02
     */
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    /**
     * 保存平台属性方法
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 19:03
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 根据属性id获取属性值
     *
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(Long attrId);

    /**
     * 根据attrId 查询平台属性对象
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrInfo(Long attrId);

    /**
     * 查询销售属性集合
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/29 17:18
     */
    List<BaseSaleAttr> baseSaleAttrList();

    /**
     * 根据spuId 查询spuImageList
     * @param spuId
     * @return
     */
    List<SpuImage> getSpuImageList(Long spuId);

    /**
     * 根据spuId 查询销售属性集合
     *
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    /**
     * 保存sku
     *
     * @param skuInfo
     * @return
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 根据三级分类数据获取到skuInfo 列表
     *
     * @param skuInfoPage
     * @param skuInfo
     * @return
     */
    IPage<SkuInfo> getSpuInfoList(Page<SkuInfo> skuInfoPage, SkuInfo skuInfo);

    /**
     * 商品上架
     *
     * @param skuId
     * @return
     */
    void onSale(Long skuId);

    /**
     * 商品下架
     *
     * @param skuId
     * @return
     */
    void cancelSale(Long skuId);
}
