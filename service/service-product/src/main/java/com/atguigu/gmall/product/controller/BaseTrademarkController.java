package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
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
 * @DATE: 2022/8/27 18:38
 */
@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {
    @Autowired
    private BaseTrademarkService baseTrademarkService;

    /**
     * 品牌分页列表
     *
     * @param
     * @return
     */
    @GetMapping("/{page}/{limit}")
    public Result<IPage<BaseTrademark>> getBaseTrademarkList(@PathVariable Long page, @PathVariable Long limit) {
        //创建分页查询对象
        Page<BaseTrademark> trademarkPage = new Page<>(page, limit);
        //调用服务层查询数据
        IPage<BaseTrademark> trademarkIPage = baseTrademarkService.getBaseTrademarkList(trademarkPage);
        //返回数据
        return Result.ok(trademarkIPage);
    }
}
