package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/13 18:41
 */
public interface OrderInfoService {
    /**
     * 保存订单
     *
     * @param orderInfo
     * @return
     */
    Long saveOrderInfo(OrderInfo orderInfo);

    /**
     * 返回流水号
     *
     * @param userId
     * @return
     */
    String getTradeNo(String userId);

    /**
     * 比较流水号
     *
     * @param tradeNo
     * @param userId
     * @return
     */
    Boolean checkTradeNo(String userId, String tradeNo);

    /**
     * 删除流水号缓存数据
     *
     * @param userId
     * @return
     */
    void deleteTradeNo(String userId);

    /**
     * 校验库存
     *
     * @param skuId
     * @param skuNum
     * @return
     */
    Boolean checkStock(Long skuId, Integer skuNum);

    /**
     * 查看我的订单
     *
     * @param orderInfoPage
     * @param userId
     * @return
     */
    IPage<OrderInfo> getMyOrderList(Page<OrderInfo> orderInfoPage, String userId);
}
