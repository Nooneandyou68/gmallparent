package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.config.ThreadPoolExecutorConfig;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jodd.util.ComparableComparator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/13 17:38
 */
@RestController
@RequestMapping("api/order")
public class OrderApiController {
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 确认订单
     *
     * @param request
     * @return
     */
    @GetMapping("auth/trade")
    public Result<Map<String, Object>> trade(HttpServletRequest request) {
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //创建返回对象
        HashMap<String, Object> map = new HashMap<>();
        //获取用户地址
        List<UserAddress> userAddressList = userFeignClient.findUserAddressListByUserId(userId);
        // 渲染送货清单
        // 先得到用户想要购买的商品！
        AtomicInteger totalNum = new AtomicInteger();
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList(userId);
        List<OrderDetail> detailList = cartCheckedList.stream().map(cartInfo -> {
            //创建订单明细对象
            OrderDetail orderDetail = new OrderDetail();
            //赋值
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setOrderId(cartInfo.getId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            //  赋值实时价格.
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            //  计算总件数
            totalNum.addAndGet(cartInfo.getSkuNum());
            return orderDetail;
        }).collect(Collectors.toList());
        //  userAddressList， detailArrayList， totalNum， totalAmount， tradeNo==交易的流水号！
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailList);
        orderInfo.sumTotalAmount();
        //放入数据
        map.put("userAddressList", userAddressList);
        map.put("detailArrayList", detailList);
        map.put("totalNum", totalNum);
        map.put("totalAmount", orderInfo.getTotalAmount());
        map.put("tradeNo", orderInfoService.getTradeNo(userId));
        //返回数据
        return Result.ok(map);
    }

    /**
     * 保存订单
     *
     * @param request
     * @return
     */
    // http://api.gmall.com/api/order/auth/submitOrder?tradeNo=34567rtyui
    @PostMapping("auth/submitOrder")
    public Result submitOrder(@RequestBody OrderInfo orderInfo, HttpServletRequest request) {
        //获取用户id
        String userId = AuthContextHolder.getUserId(request);
        //放入用户id
        orderInfo.setUserId(Long.parseLong(userId));
        // 获取页面传递的流水号
        String tradeNo = request.getParameter("tradeNo");
        //比较流水号
        Boolean result = orderInfoService.checkTradeNo(userId, tradeNo);
        //  表示比较失败!
        if (!result) {
            return Result.fail().message("不能重复无刷新回退提交订单.");
        }
        //  删除流水号
        this.orderInfoService.deleteTradeNo(userId);
        // 校验库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //声明一个多线程集合
        ArrayList<CompletableFuture> completableFutureList = new ArrayList<>();
        //声明string类型的集合来存储信息提示
        ArrayList<String> errorList = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetailList) {
            //使用多线程异步编排
            Integer skuNum = orderDetail.getSkuNum();
            Long skuId = orderDetail.getSkuId();
            // 这里不需要返回值 因为return会终止其他
            CompletableFuture<Void> checkStockCompletableFuture = CompletableFuture.runAsync(() -> {
                //  调用校验库存系统接口
                Boolean exist = this.orderInfoService.checkStock(skuId, skuNum);
                if (!exist) {
                    errorList.add(orderDetail.getSkuId() + "库存不足!");
                }
            }, threadPoolExecutor);
            //添加到多线程集合
            completableFutureList.add(checkStockCompletableFuture);

            // 异步编排查询订单价格
            CompletableFuture<Void> priceCompletable = CompletableFuture.runAsync(() -> {
                //校验价格：订单价格
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                // 商品的实时价格
                BigDecimal skuPrice = this.productFeignClient.getSkuPrice(skuId);
                // 判断是否涨价或者降价
                if (orderPrice.compareTo(skuPrice) == 0) {
                    //没有变动
                    String msg = orderPrice.compareTo(skuPrice) > 0 ? "降价" : "涨价";
                    // 变动价格，取绝对值
                    BigDecimal price = orderPrice.subtract(skuPrice).abs();
                    // 自动更新购物车价格
                    String cartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
                    // 更新价格之前先查询
                    CartInfo cartInfo = (CartInfo) this.redisTemplate.opsForHash().get(cartKey, skuId.toString());
                    cartInfo.setSkuPrice(skuPrice);
                    // 更新缓存
                    this.redisTemplate.opsForHash().put(cartKey, orderDetail.getSkuId().toString(), cartInfo);
                    // 返回信息
                    errorList.add(orderDetail.getSkuId() + "价格：" + msg + price + "元");
                }
            }, threadPoolExecutor);
            //添加到多线程集合
            completableFutureList.add(priceCompletable);
        }
        //多任务组合，所有的异步编排对象都在集合中
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[completableFutureList.size()])).join();
        // 判断是否有错误出现
        if (errorList.size() > 0) {
            // 将集合中的数据 使用逗号进行拼接成字符串
            String msg = StringUtils.join(errorList, ",");
            // 返回消息信息
            return Result.fail().message(msg);
        }
        //  调用服务层方法
        Long orderId = this.orderInfoService.saveOrderInfo(orderInfo);
        //  返回订单Id
        return Result.ok(orderId);
    }

    /**
     * 查看我的订单
     *
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("auth/{page}/{limit}")
    public Result<IPage<OrderInfo>> getOrderPageList(@PathVariable Long page,
                                                     @PathVariable Long limit,
                                                     HttpServletRequest request) {
        // 获取用户Id
        String userId = AuthContextHolder.getUserId(request);
        // 创建page对象
        Page<OrderInfo> orderInfoPage = new Page<>(page, limit);
        // 调用服务层对象
        IPage<OrderInfo> iPage = this.orderInfoService.getMyOrderList(orderInfoPage, userId);
        //返回数据
        return Result.ok(iPage);
    }
}
