package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/27 18:15
 */
@RestController//组合注解@ResponseBody 返回json数据 @Controller
@RequestMapping("admin/product/")
public class SpuManageController {
    @Autowired
    private SpuInfoService spuInfoService;

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
        Page<SpuInfo> spuInfoPage = new Page<>(page,limit);
        IPage<SpuInfo> spuInfoIPage = spuInfoService.getSpuList(spuInfoPage, spuInfo);
        return Result.ok(spuInfoIPage);
    }
}
