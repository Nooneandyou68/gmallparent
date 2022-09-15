package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/9 16:22
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;

    // 获取购物车的key
    private String getCartKey(String userId) {
        //定义key user:userId:cart
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }

    /**
     * 添加购物车 用户Id，商品Id，商品数量。
     *
     * @param skuId
     * @param userId
     * @return skuNum
     * @author SongBoHao
     * @date 2022/9/9 16:23
     */
    @Override
    public void addToCart(Long skuId, String userId, Integer skuNum) {
        //获取cartKey
        String cartKey = this.getCartKey(userId);
        //获取购物车项
        CartInfo cartInfoExist = (CartInfo) redisTemplate.opsForHash().get(cartKey, skuId.toString());
        //判断是否存在
        if (cartInfoExist != null) {
            //有这个购物项 保存商品数量不能超过200
            if (cartInfoExist.getSkuNum() + skuNum > 200) {
                cartInfoExist.setSkuNum(200);
            } else {
                cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + skuNum);
            }
            //显示实时价格
            cartInfoExist.setSkuPrice(productFeignClient.getSkuPrice(skuId));
            // 设置选中状态
            if (cartInfoExist.getIsChecked().intValue() == 0) {
                cartInfoExist.setIsChecked(1);
            }
            //设置一个修改时间
            cartInfoExist.setUpdateTime(new Date());

        } else {
            // 当前商品不存在！
            cartInfoExist = new CartInfo();
            //根据skuId 来获取 skuInfo
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

            cartInfoExist.setSkuId(skuId);
            cartInfoExist.setUserId(userId);
            cartInfoExist.setSkuNum(skuNum);
            //实时价格
            cartInfoExist.setSkuPrice(productFeignClient.getSkuPrice(skuId));
            // 加入购物车的价格
            cartInfoExist.setCartPrice(productFeignClient.getSkuPrice(skuId));
            cartInfoExist.setSkuName(skuInfo.getSkuName());
            cartInfoExist.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfoExist.setCreateTime(new Date());
            cartInfoExist.setUpdateTime(new Date());
        }
        //存储到缓存
        this.redisTemplate.opsForHash().put(cartKey, skuId.toString(), cartInfoExist);

    }

    /**
     * 查询购物车
     *
     * @param
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        //声明一个集合列表
        // 未登录集合列表
        List<CartInfo> cartInfoNoLoginList = new ArrayList<>();
        //判断临时用户id不为空
        if (!StringUtils.isEmpty(userTempId) && !StringUtils.isEmpty(userId)) {
            //获取缓存key
            String cartKey = this.getCartKey(userTempId);
            // 查询缓存数据
            cartInfoNoLoginList = this.redisTemplate.opsForHash().values(cartKey);
            if (CollectionUtils.isEmpty(cartInfoNoLoginList)) {
                //排序
                cartInfoNoLoginList.sort((o1, o2) -> DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND));
                //返回未登录购物车集合数据
                return cartInfoNoLoginList;
            }
        }
        //创建一个登录的购物车集合
        List<CartInfo> cartInfoLoginList = new ArrayList<>();
        // 判断获取到用户登录的购物车数据
        if (!StringUtils.isEmpty(userId)) {
            //获取缓存key
            String cartKey = this.getCartKey(userId);
            // 获取数据
            BoundHashOperations<String, String, CartInfo> hashOperations = this.redisTemplate.boundHashOps(cartKey);
            // hget key field = hashOperations.get()
            // hvals key = hashOperations.values();
            // 未登录数据不为空
            if (!CollectionUtils.isEmpty(cartInfoNoLoginList)) {
                //合并
                cartInfoNoLoginList.forEach(cartInfoNoLogin -> {
                    //判断未登录数据是否在已登录数据中存在
                    if (hashOperations.hasKey(cartInfoNoLogin.getSkuId().toString())) {
                        // skuNum 相加
                        CartInfo cartInfoLogin = hashOperations.get(cartInfoNoLogin.getSkuId());
                        // 判断skuNum是否超过200
                        if (cartInfoLogin.getSkuNum() + cartInfoNoLogin.getSkuNum() > 200) {
                            cartInfoLogin.setSkuNum(200);
                        } else {
                            cartInfoLogin.setSkuNum(cartInfoLogin.getSkuNum() + cartInfoNoLogin.getSkuNum());
                        }
                        // 判断选中状态 : 未登录状态
                        if (cartInfoNoLogin.getIsChecked().intValue() == 1) {
                            //登录状态
                            if (cartInfoLogin.getIsChecked().intValue() == 0) {
                                cartInfoLogin.setIsChecked(1);
                            }
                        }
                        // 相当于修改了 重新设置修改时间
                        cartInfoLogin.setUpdateTime(new Date());
                        //写回缓存
                        this.redisTemplate.boundHashOps(cartKey).put(cartInfoLogin.getSkuId().toString(), cartInfoLogin);
                    } else {
                        //未找到相同数据 直接加入缓存
                        if (cartInfoNoLogin.getIsChecked().intValue() == 1) {
                            cartInfoNoLogin.setUserId(userId);
                            cartInfoNoLogin.setCreateTime(new Date());
                            cartInfoNoLogin.setUpdateTime(new Date());
                            this.redisTemplate.opsForHash().put(cartKey, cartInfoNoLogin.getSkuId().toString(), cartInfoNoLogin);
                        }

                    }
                });
                //删除未登录购物车
                this.redisTemplate.delete(this.getCartKey(userTempId));
            }
            //查询所有
            cartInfoLoginList = hashOperations.values();
        }
        //如果购物车数据为空则返回空集合 不为空排序返回集合数据
        if (CollectionUtils.isEmpty(cartInfoLoginList)) {
            return new ArrayList<>();
        }
        //排序
        cartInfoLoginList.sort((o1, o2) -> DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND));
        //返回数据
        return cartInfoLoginList;
        /*
        //创建一个登录的购物车集合
        List<CartInfo> cartInfoLoginList = new ArrayList<>();
        //判断用户id不为空
        if (!StringUtils.isEmpty(userId)) {
            //说明是登录
            //获取缓存key
            String cartKey = this.getCartKey(userId);
            // 查询缓存数据
            cartInfoLoginList = this.redisTemplate.opsForHash().values(cartKey);
            //判断tempId 是否为空
            if (!StringUtils.isEmpty(userTempId)) {
                //如果不为空 则合并数据
                List<CartInfo> finalCartInfoLoginList = cartInfoLoginList;
                cartInfoNoLoginList.forEach(cartInfoNoLogin -> {
                    finalCartInfoLoginList.forEach(cartInfoLogin -> {
                        // 判断登录和未登录 sku是否都存在
                        if (cartInfoNoLogin.getSkuId().compareTo(cartInfoLogin.getSkuId()) == 0) {
                            //数量相加
                            cartInfoLogin.setSkuNum(cartInfoLogin.getSkuNum() + cartInfoNoLogin.getSkuNum());
                            //同步到缓存
                            this.redisTemplate.opsForHash().put(cartKey, cartInfoNoLogin.getSkuId().toString(), cartInfoLogin);
                        } else {
                            //直接加入缓存
                            cartInfoNoLogin.setUserId(cartInfoLogin.getUserId());
                            cartInfoNoLogin.setCreateTime(new Date());
                            cartInfoNoLogin.setUpdateTime(new Date());
                            this.redisTemplate.opsForHash().put(cartKey, cartInfoNoLogin.getSkuId().toString(), cartInfoNoLogin);
                        }
                    });
                });
                // 删除临时购物车数据
                this.redisTemplate.delete(getCartKey(userTempId));
                //查询合并之后的数据
                cartInfoLoginList = this.redisTemplate.opsForHash().values(cartKey);
                //排序
                cartInfoLoginList.sort((o1, o2) -> DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND));
                //返回数据
                return cartInfoLoginList;
            } else {
                //排序
                cartInfoLoginList.sort((o1, o2) -> DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND));
                //返回数据
                return cartInfoLoginList;
            }
        }*/
        //返回数据
        //return null;
    }

    /**
     * 更改选中状态
     *
     * @param
     * @return
     */
    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        // 获取key
        String cartKey = this.getCartKey(userId);
        //查询数据
        CartInfo cartInfo = (CartInfo) this.redisTemplate.opsForHash().get(cartKey, skuId.toString());
        if (cartInfo != null) {
            //放入选中状态
            cartInfo.setIsChecked(isChecked);
            //写回数据
            this.redisTemplate.opsForHash().put(cartKey, skuId.toString(), cartInfo);
        }
    }

    /**
     * 删除
     *
     * @param skuId
     * @param userId
     * @return
     */
    @Override
    public void deleteCart(Long skuId, String userId) {
        // 获取key
        String cartKey = this.getCartKey(userId);
        //查询数据
        this.redisTemplate.opsForHash().delete(cartKey, skuId.toString());
    }

    /**
     * 根据用户Id 查询购物车列表
     *
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        //获取购物车key
        String cartKey = this.getCartKey(userId);
        //获取数据
        List<CartInfo> cartInfoList = this.redisTemplate.opsForHash().values(cartKey);
        //过滤数据并返回
        return cartInfoList.stream().filter(cartInfo -> cartInfo.getIsChecked().intValue() == 1).collect(Collectors.toList());
    }
}
