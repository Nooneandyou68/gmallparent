package com.atguigu.gmall.user.client.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/13 16:50
 */
@Component
public class UserDegradeFeignClient implements UserFeignClient {

    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {
        return null;
    }
}
