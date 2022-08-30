package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuImage;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.ManageService;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/30 16:41
 */
@RestController
@RequestMapping("admin/product")
public class SkuManageController {
    //http://localhost/admin/product/attrInfoList/2/13/61
    @Autowired
    private ManageService manageService;
    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * 根据spuId 查询spuImageList
     * @param spuId
     * @return
     */
    @GetMapping("spuImageList/{spuId}")
    public Result<List<SpuImage>> getSpuImageList(@PathVariable("spuId") Long spuId) {
        List<SpuImage> spuImageList = this.manageService.getSpuImageList(spuId);
        return Result.ok(spuImageList);
    }

    /**
     * 根据spuId 查询销售属性集合
     *
     * @param spuId
     * @return
     */
    // http://localhost/admin/product/spuSaleAttrList/11
    @GetMapping("spuSaleAttrList/{spuId}")
    public Result<List<SpuSaleAttr>> getSpuSaleAttrList(@PathVariable Long spuId) {
        //调用服务层方法
        List<SpuSaleAttr> spuSaleAttrList = this.manageService.getSpuSaleAttrList(spuId);
        //返回数据
        return Result.ok(spuSaleAttrList);
    }

    /**
     * 保存sku
     *
     * @param skuInfo
     * @return
     */
    //http://localhost/admin/product/saveSkuInfo
    @PostMapping("saveSkuInfo")
    public Result<SkuInfo> saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        //调用服务层方法
        this.manageService.saveSkuInfo(skuInfo);
        //返回默认数据
        return Result.ok();
    }

    /**
     * 根据三级分类数据获取到skuInfo 列表
     *
     * @param page
     * @param skuInfo
     * @return
     */
    //http://localhost/admin/product/list/1/10?category3Id=61
    @GetMapping("list/{page}/{limit}")
    public Result<IPage<SkuInfo>> getSpuInfoList(@PathVariable Long page,
                                   @PathVariable Long limit,
                                   SkuInfo skuInfo) {
        Page<SkuInfo> skuInfoPage = new Page<>(page, limit);
        IPage<SkuInfo> skuInfoIPage = this.manageService.getSpuInfoList(skuInfoPage, skuInfo);
        return Result.ok(skuInfoIPage);
    }

    /**
     * 商品上架
     *
     * @param skuId
     * @return
     */
    ///admin/product/onSale/{skuId}
    @GetMapping("onSale/{skuId}")
    public Result<SkuInfo> onSale(@PathVariable Long skuId) {
        this.manageService.onSale(skuId);
        return Result.ok();
    }
    /**
     * 商品下架
     *
     * @param skuId
     * @return
     */
    ///admin/product/cancelSale/{skuId}
    @GetMapping("cancelSale/{skuId}")
    public Result<SkuInfo> cancelSale(@PathVariable Long skuId) {
        this.manageService.cancelSale(skuId);
        return Result.ok();
    }

    /**
     * 修改sku数据回显
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/30 20:13
     */
    //http://localhost/admin/product/getSkuInfo/18
    @GetMapping("getSkuInfo/{id}")
    public Result<SkuInfo> getSkuInfo(@PathVariable Long id) {
        //调用服务层
        SkuInfo skuInfo = skuInfoService.getSkuInfo(id);
        //返回数据
        return Result.ok(skuInfo);
    }

    /**
     * 保存修改sku
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/30 23:48
     */
    //http://localhost/admin/product/updateSkuInfo
    @PostMapping("updateSkuInfo")
    public Result<SkuInfo> updateSkuInfo(@RequestBody SkuInfo skuInfo) {
        this.skuInfoService.updateSkuInfo(skuInfo);
        return Result.ok();
    }
}
