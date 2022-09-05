package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/31 16:22
 */
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    /**
     * 获取sku详情信息
     *
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getBySkuId(Long skuId) {
        Map<String, Object> result = new HashMap<>();
        //添加布隆过滤
        /*RBloomFilter<Long> rbloomFilter = redissonClient.getBloomFilter(RedisConst.SKU_BLOOM_FILTER);
        //不包含
        if(!rbloomFilter.contains(skuId)) return result;*/
        //获取商品基本信息+商品图片列表
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            //保存skuInfo
            result.put("skuInfo",skuInfo);
            return skuInfo;
        },threadPoolExecutor);
        //获取分类数据
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            result.put("categoryView", categoryView);
        },threadPoolExecutor);
        //获取最新价格
        CompletableFuture<Void> productCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuInfo.getId());
            result.put("price", skuPrice);
        },threadPoolExecutor);
        //获取销售属性+属性值+锁定
        CompletableFuture<Void> spuSaleAttrCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            result.put("spuSaleAttrList", spuSaleAttrList);
        },threadPoolExecutor);
        //获取海报
        CompletableFuture<Void> posterCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuPoster> spuPosterList = productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());
            result.put("spuPosterList", spuPosterList);
        },threadPoolExecutor);
        //获取json字符串Raw use of parameterized class 'Map'
        CompletableFuture<Void> strJsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            //map转json字符串
            String strJson = JSON.toJSONString(skuValueIdsMap);
            result.put("valuesSkuJson", strJson);
        },threadPoolExecutor);
        //获取商品规格参数--平台属性
        CompletableFuture<Void> BaseAttrInfoCompletableFuture = CompletableFuture.runAsync(() -> {
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            if (!CollectionUtils.isEmpty(attrList)) {
                List<HashMap<String, Object>> mapList = attrList.stream().map(baseAttrInfo -> {
                    //  为了迎合页面数据存储，定义一个map 集合
                    HashMap<String, Object> map = new HashMap<>();
                    //将map看做一个java对象
                    map.put("attrName", baseAttrInfo.getAttrName());
                    map.put("attrValue", baseAttrInfo.getAttrValueList().get(0).getValueName());
                    return map;
                }).collect(Collectors.toList());
                //保存规格参数：只需要平台属性名称：平台属性值名称
                result.put("skuAttrList", mapList);
            }
        },threadPoolExecutor);
        CompletableFuture.allOf(
                skuInfoCompletableFuture,
                categoryViewCompletableFuture,
                productCompletableFuture,
                spuSaleAttrCompletableFuture,
                posterCompletableFuture,
                strJsonCompletableFuture,
                BaseAttrInfoCompletableFuture
        ).join();
        //返回数据
        return result;
    }
}
