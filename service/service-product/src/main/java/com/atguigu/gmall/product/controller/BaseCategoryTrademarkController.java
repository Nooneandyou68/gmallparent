package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/28 16:48
 */
@RestController
@RequestMapping("/admin/product/baseCategoryTrademark")
public class BaseCategoryTrademarkController {

    @Autowired
    private BaseCategoryTrademarkService baseCategoryTrademarkService;

    /**
     * 查询分类品牌列表/admin/product/baseCategoryTrademark/findTrademarkList/{category3Id}
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 16:51
     */
    @GetMapping("findTrademarkList/{category3Id}")
    public Result<List<BaseTrademark>> findTrademarkList(@PathVariable Long category3Id) {
        //根据3级id查询品牌列表
        List<BaseTrademark> baseTrademarkList = this.baseCategoryTrademarkService.findTrademarkList(category3Id);
        //返回数据
        return Result.ok(baseTrademarkList);
    }

    /**
     * 分类品牌管理/admin/product/baseCategoryTrademark/findCurrentTrademarkList/{category3Id}
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 23:04
     */
    @GetMapping("findCurrentTrademarkList/{category3Id}")
    public Result<List<BaseTrademark>> findCurrentTrademarkList(@PathVariable Long category3Id) {
        List<BaseTrademark> baseTrademarkList = baseCategoryTrademarkService.findCurrentTrademarkList(category3Id);
        return Result.ok(baseTrademarkList);
    }

    /**
     * 删除品牌关联/admin/product/baseCategoryTrademark/remove/{category3Id}/{trademarkId}
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 23:11
     */
    @DeleteMapping("remove/{category3Id}/{trademarkId}")
    public Result remove(@PathVariable Long category3Id, @PathVariable Long trademarkId) {
        this.baseCategoryTrademarkService.removeBaseCategoryTrademarkById(category3Id, trademarkId);
        return Result.ok();
    }

    /**
     * 保存分类品牌关联/admin/product/baseCategoryTrademark/save
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/29 16:29
     */
    @PostMapping("save")
    public Result<BaseCategoryTrademark> save(@RequestBody CategoryTrademarkVo categoryTrademarkVo) {
        //接收数据保存
        this.baseCategoryTrademarkService.save(categoryTrademarkVo);
        //返回成功
        return Result.ok();
    }

}
