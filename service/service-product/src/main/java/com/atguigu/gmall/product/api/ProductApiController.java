package com.atguigu.gmall.product.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.atguigu.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/31 16:29
 */
@RestController
@RequestMapping("api/product")
public class ProductApiController {
    @Autowired
    private ManageService manageService;
    @Autowired
    private BaseTrademarkService baseTrademarkService;
    /**
     * 根据skuId获取skuInfo
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/31 16:32
     */
    ///api/product/inner/getSkuInfo/{skuId}
    @GetMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId) {
        return manageService.getSkuInfo(skuId);
    }

    /**
     * 根据skuId获取海报图片
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/31 16:39
     */
    ///api/product/inner/findSpuPosterBySpuId/{spuId}
    @GetMapping("inner/findSpuPosterBySpuId/{spuId}")
    public List<SpuPoster> findSpuPosterBySpuId(@PathVariable Long spuId) {
        return manageService.findSpuPosterBySpuId(spuId);
    }

    /**
     * 根据skuId获取最新商品价格
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/31 16:45
     */
    ///api/product/inner/getSkuPrice/{skuId}
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId) {
        return this.manageService.getSkuPrice(skuId);
    }

    /**
     * 通过三级分类id查询分类信息
     *
     * @param category3Id
     * @return
     */
    ///api/product/inner/getCategoryView/{category3Id}
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id) {
        return this.manageService.getCategoryViewByCategory3Id(category3Id);
    }

    /**
     * 根据spuId，skuId 查询销售属性集合
     *
     * @param skuId
     * @param spuId
     * @return
     */
    @GetMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId) {
        return this.manageService.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    /**
     * 根据spuId 查询map 集合属性
     *
     * @param spuId
     * @return
     */
    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId) {
        return this.manageService.getSkuValueIdsMap(spuId);
    }

    /**
     * 通过skuId 集合来查询数据
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable("skuId") Long skuId) {
        return manageService.getAttrList(skuId);
    }

    /**
     * 获取全部分类信息
     *
     * @return
     */
    @GetMapping("getBaseCategoryList")
    public Result<List<JSONObject>> getBaseCategoryList() {
        List<JSONObject> list = manageService.getBaseCategoryList();
        return Result.ok(list);
    }

    /**
     * 查询 sku 对应的品牌信息！ tmId 可以从skuInfo 获取！
     *
     * @return
     */
    @GetMapping("inner/getTrademark/{tmId}")
    public BaseTrademark getTradeMark(@PathVariable Long tmId) {
        //  select * from base_trademark where id = ?;
        return baseTrademarkService.getById(tmId);
    }
}
