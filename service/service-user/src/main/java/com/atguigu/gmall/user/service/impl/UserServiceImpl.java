package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/8 14:07
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录方法
     *
     * @param userInfo
     * @return
     */
    @Override
    public UserInfo login(UserInfo userInfo) {
        if (userInfo != null) {
            //设置查询条件
            QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
            //设置md5加密
            String newPassword = MD5.encrypt(userInfo.getPasswd());
            userInfoQueryWrapper.eq("login_name", userInfo.getLoginName());
            userInfoQueryWrapper.eq("passwd", newPassword);
            //根据筛选条件查询数据并返回
            return userMapper.selectOne(userInfoQueryWrapper);
        } else {
            return null;
        }
    }
}
