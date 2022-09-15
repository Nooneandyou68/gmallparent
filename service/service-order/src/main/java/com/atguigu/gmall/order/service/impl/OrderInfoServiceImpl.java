package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/13 18:41
 */
@Service
public class OrderInfoServiceImpl implements OrderInfoService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${ware.url}")
    private String wareUrl;

    /**
     * 保存订单
     *
     * @param orderInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrderInfo(OrderInfo orderInfo) {
        // 添加总金额
        orderInfo.sumTotalAmount();
        //添加订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        // 订单的描述信息： 将商品的名称定义为订单的描述信息.
        orderInfo.setTradeBody("购买国产手机咔咔咔香");
        //第三方交易编号
        String tradeNo = "SPH" + System.currentTimeMillis() + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(tradeNo);
        //添加订单创建时间
        orderInfo.setOperateTime(new Date());
        //添加失效时间 所有的商品默认为24小时:
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
        //添加进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        orderInfoMapper.insert(orderInfo);
        //获取orderDetail集合数据并添加到数据库
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            orderDetailList.forEach(orderDetail -> {
                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insert(orderDetail);
            });
        }
        return orderInfo.getId();
    }

    /**
     * 返回流水号
     *
     * @param userId
     * @return
     */
    @Override
    public String getTradeNo(String userId) {
        // 声明一个缓存keu
        String key = "tradeNo:" + userId;
        // 创建一个流水号
        String tradeNo = UUID.randomUUID().toString();
        // 存储到redis中
        this.redisTemplate.opsForValue().set(key, tradeNo);
        return tradeNo;
    }

    @Override
    public Boolean checkTradeNo(String userId, String tradeNo) {
        // 声明一个缓存keu
        String key = "tradeNo:" + userId;
        // 获取缓存数据
        String redisTradeNo = (String) this.redisTemplate.opsForValue().get(key);
        //比较返回结果
        return tradeNo.equals(redisTradeNo);
    }

    /**
     * 删除流水号缓存数据
     *
     * @param userId
     * @return
     */
    @Override
    public void deleteTradeNo(String userId) {
        // 声明一个缓存keu
        String key = "tradeNo:" + userId;
        // 删除缓存数据
        this.redisTemplate.delete(key);
    }

    @Override
    public Boolean checkStock(Long skuId, Integer skuNum) {
        //  远程调用库存系统接口: http://localhost:9001/hasStock?skuId=10221&num=2
        // http://localhost:9001
        String res = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        //  返回比较结果。
        return "1".equals(res);
    }

    /**
     * 查看我的订单
     *
     * @param orderInfoPage
     * @param userId
     * @return
     */
    @Override
    public IPage<OrderInfo> getMyOrderList(Page<OrderInfo> orderInfoPage, String userId) {
        IPage<OrderInfo> orderInfoIPage = this.orderInfoMapper.selectMyOrderList(orderInfoPage, userId);
        orderInfoIPage.getRecords().forEach(orderInfo -> {
            String statusNameByStatus = OrderStatus.getStatusNameByStatus(orderInfo.getOrderStatus());
            orderInfo.setOrderStatusName(statusNameByStatus);
        });
        return orderInfoIPage;
    }
}
