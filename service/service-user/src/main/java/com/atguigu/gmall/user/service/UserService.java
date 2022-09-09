package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/8 14:07
 */
public interface UserService {

    /**
     * 登录方法
     *
     * @param userInfo
     * @return
     */
    UserInfo login(UserInfo userInfo);

}
