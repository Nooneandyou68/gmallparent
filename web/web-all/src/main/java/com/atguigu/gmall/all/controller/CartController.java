package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/11 23:38
 */
@Controller
public class CartController {
    @Autowired
    private ProductFeignClient productFeignClient;

    /**
     * 添加购物车
     *
     * @param request
     * @return
     */
    @GetMapping("addCart.html")
    public String addCart(HttpServletRequest request) {
        // 获取skuId
        String skuId = request.getParameter("skuId");
        // 远程调用获取skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(Long.parseLong(skuId));
        // 添加到请求域
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", request.getParameter("skuNum"));
        return "cart/addCart";
    }
    /**
     * 查看购物车列表
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String getCartList() {
        //返回购物车列表
        return "cart/index";
    }
}
