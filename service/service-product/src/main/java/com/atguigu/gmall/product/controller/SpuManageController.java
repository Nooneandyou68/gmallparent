package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.ManageService;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/27 18:15
 */
@RestController//组合注解@ResponseBody 返回json数据 @Controller
@RequestMapping("admin/product/")
public class SpuManageController {
    // 引入服务层
    @Autowired
    private SpuInfoService spuInfoService;
    @Autowired
    private ManageService manageService;

    /**
     * 根据三级分类Id 查询spu 列表！
     *
     * @param page  第几页
     * @param limit 每页显示的条数
     * @return
     */
    @GetMapping("/{page}/{limit}")
    public Result<IPage<SpuInfo>> getSpuList(@PathVariable Long page,
                                             @PathVariable Long limit,
                                             SpuInfo spuInfo) {
        Page<SpuInfo> spuInfoPage = new Page<>(page, limit);
        IPage<SpuInfo> spuInfoIPage = spuInfoService.getSpuList(spuInfoPage, spuInfo);
        return Result.ok(spuInfoIPage);
    }

    @GetMapping("baseSaleAttrList")
    public Result<List<BaseSaleAttr>> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = this.manageService.baseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }

    /**
     * 保存spu
     */

    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        spuInfoService.saveSpuInfo(spuInfo);
        return Result.ok();
    }

    /**
     * 修改数据回显
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/30 19:31
     */
    //http://localhost/admin/product/getSpuInfo/
    @GetMapping("getSpuInfo/{spuId}")
    public Result<SpuInfo> getSpuInfo(@PathVariable Long spuId) {
        SpuInfo spuInfo = this.spuInfoService.getSpuInfo(spuId);
        return Result.ok(spuInfo);
    }

    /**
     * 修改保存
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/30 19:31
     */
    //http://localhost/admin/product/updateSpuInfo
    @PostMapping("updateSpuInfo")
    public Result<SpuInfo> updateSpuInfo(@RequestBody SpuInfo spuInfo) {
        this.spuInfoService.updateSpuInfo(spuInfo);
        return Result.ok();
    }
}
