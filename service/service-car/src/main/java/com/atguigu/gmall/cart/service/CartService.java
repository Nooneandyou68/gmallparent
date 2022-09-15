package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/9 16:22
 */
public interface CartService {

    // 添加购物车 用户Id，商品Id，商品数量。
    void addToCart(Long skuId, String userId, Integer skuNum);

    /**
     * 查询购物车
     *
     * @param
     * @return
     */
    List<CartInfo> getCartList(String userId, String userTempId);

    /**
     * 更改选中状态
     *
     * @param
     * @return
     */
    void checkCart(String userId, Integer isChecked, Long skuId);

    /**
     * 删除
     *
     * @param skuId
     * @param userId
     * @return
     */
    void deleteCart(Long skuId, String userId);

    /**
     * 根据用户Id 查询购物车列表
     *
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);

}
