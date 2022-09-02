package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * 获取sku详情信息
     *
     * @param skuId
     * @return
     */
    @Override
    public Map<String, Object> getBySkuId(Long skuId) {
        Map<String, Object> result = new HashMap<>();
        //获取商品基本信息+商品图片列表
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        //获取分类数据
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        //获取最新价格
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuInfo.getId());
        //获取销售属性+属性值+锁定
        List<SpuSaleAttr> spuSaleAttrList = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
        //获取海报
        List<SpuPoster> spuPosterList = productFeignClient.findSpuPosterBySpuId(skuInfo.getSpuId());
        //获取json字符串Raw use of parameterized class 'Map'
        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
        //map转json字符串
        String strJson = JSON.toJSONString(skuValueIdsMap);
        //获取商品规格参数--平台属性
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
        //key是页面渲染时需要的key
        result.put("skuInfo",skuInfo);
        result.put("categoryView",categoryView);
        result.put("price",skuPrice);
        result.put("spuSaleAttrList",spuSaleAttrList);
        result.put("spuPosterList",spuPosterList);
        result.put("valuesSkuJson",strJson);
        //返回数据
        return result;
    }
}
