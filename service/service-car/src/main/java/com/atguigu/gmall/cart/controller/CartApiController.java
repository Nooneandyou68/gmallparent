package com.atguigu.gmall.cart.controller;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/9 20:29
 */
@RestController
@RequestMapping("api/cart")
public class CartApiController {

    @Autowired  
    private CartService cartService;

    /**
     * 添加购物车
     *
     * @param skuId
     * @param skuNum
     * @param request
     * @return
     */
    @GetMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                            @PathVariable("skuNum") Integer skuNum,
                            HttpServletRequest request) {
        //获取userId
        String userId = AuthContextHolder.getUserId(request);
        //判断用户id是否存在
        if (StringUtils.isEmpty(userId)) {
            // 如果不存在获取临时用户id
            userId = AuthContextHolder.getUserTempId(request);
        }
        System.out.println("===>" + userId + "<=====");
            //调用服务层方法
            cartService.addToCart(skuId, userId, skuNum);

        return Result.ok();
    }

    /**
     * 查询购物车
     *
     * @param request
     * @return
     */
    @GetMapping("cartList")
    public Result getCartList(HttpServletRequest request) {
        //获取userId
        String userId = AuthContextHolder.getUserId(request);
        // 获取临时用户id
        String userTempId = AuthContextHolder.getUserTempId(request);
        // 调用服务层方法
        List<CartInfo> cartInfoList = cartService.getCartList(userId, userTempId);
        // 返回数据
        return Result.ok(cartInfoList);
    }

    /**
     * 更改选中状态
     *
     * @param
     * @return
     */
    @GetMapping("checkCart/{skuId}/{isChecked}")
    public Result checkCart(@PathVariable Long skuId,
                            @PathVariable Integer isChecked,
                            HttpServletRequest request) {
        //获取userId
        String userId = AuthContextHolder.getUserId(request);
        //判断用户id是否存在
        if (StringUtils.isEmpty(userId)) {
            // 如果不存在获取临时用户id
            userId = AuthContextHolder.getUserTempId(request);
        }
        //  调用服务层方法
        cartService.checkCart(userId, isChecked, skuId);
        return Result.ok();
    }

    /**
     * 删除
     *
     * @param skuId
     * @param request
     * @return
     */
    @DeleteMapping("deleteCart/{skuId}")
    public Result deleteCart(@PathVariable("skuId") Long skuId,
                             HttpServletRequest request) {
        // 如何获取userId
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            // 获取临时用户Id
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.deleteCart(skuId, userId);
        return Result.ok();
    }

}