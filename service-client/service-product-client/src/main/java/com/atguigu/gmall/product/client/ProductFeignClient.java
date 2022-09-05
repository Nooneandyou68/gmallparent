package com.atguigu.gmall.product.client;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.impl.ProductFeignClientImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/1 18:14
 */
@FeignClient(value = "service-product", fallback = ProductFeignClientImpl.class)
public interface ProductFeignClient {

    /**
     * 根据skuId获取skuInfo
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/31 16:32
     */
    ///api/product/inner/getSkuInfo/{skuId}
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    SkuInfo getSkuInfo(@PathVariable Long skuId);

    /**
     * 根据skuId获取海报图片
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/31 16:39
     */
    ///api/product/inner/findSpuPosterBySpuId/{spuId}
    @GetMapping("/api/product/inner/findSpuPosterBySpuId/{spuId}")
    List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId);


    /**
     * 根据skuId获取最新商品价格
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/31 16:45
     */
    ///api/product/inner/getSkuPrice/{skuId}
    @GetMapping("/api/product/inner/getSkuPrice/{skuId}")
    BigDecimal getSkuPrice(@PathVariable Long skuId);


    /**
     * 通过三级分类id查询分类信息
     *
     * @param category3Id
     * @return
     */
    ///api/product/inner/getCategoryView/{category3Id}
    @GetMapping("/api/product/inner/getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable Long category3Id);


    /**
     * 根据spuId，skuId 查询销售属性集合
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId);


    /**
     * 根据spuId 查询map 集合属性
     *
     * @param spuId
     * @return
     */
    @GetMapping("/api/product/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId);

    /**
     * 通过skuId 集合来查询数据
     *
     * @param skuId
     * @return
     */
    @GetMapping("/api/product/inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable("skuId") Long skuId);

    /**
     * 获取全部分类信息
     *
     * @return
     */
    @GetMapping("api/product/getBaseCategoryList")
    Result<List<JSONObject>> getBaseCategoryList();
}
