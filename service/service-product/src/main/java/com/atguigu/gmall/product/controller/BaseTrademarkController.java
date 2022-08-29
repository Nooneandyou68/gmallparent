package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 添加品牌
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 10:13
     */
    //http://139.198.127.41:3000/mock/11/admin/product/baseTrademark/save
    @PostMapping("save")
    public Result<BaseTrademark> save(@RequestBody BaseTrademark baseTrademark) {
        this.baseTrademarkService.save(baseTrademark);
        return Result.ok();
    }

    /**
     * 删除品牌
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 16:24
     */
    //baseTrademark/remove/{id}
    @DeleteMapping("/remove/{id}")
    public Result<BaseTrademark> remove(@PathVariable Long id) {
        this.baseTrademarkService.removeById(id);
        return Result.ok();
    }

    /**
     * 修改品牌/admin/product/baseTrademark/update
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 16:40
     */
    @PutMapping("update")
    public Result<BaseTrademark> update(@RequestBody BaseTrademark baseTrademark) {
        this.baseTrademarkService.updateById(baseTrademark);
        return Result.ok();
    }

    /**
     * 根据品牌Id 获取品牌对象/admin/product/baseTrademark/get/{id}
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 16:44
     */
    @GetMapping("get/{id}")
    public Result<BaseTrademark> get(@PathVariable Long id) {
        BaseTrademark baseTrademark = this.baseTrademarkService.getById(id);
        return Result.ok(baseTrademark);
    }
}
