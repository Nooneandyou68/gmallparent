package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/13 16:29
 */
public interface UserAddressService {

    /**
     * 获取用户地址
     *
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressListByUserId(String userId);


}
