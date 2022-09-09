package com.atguigu.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/8 14:06
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录
     *
     * @param userInfo
     * @param
     * @return
     */

    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request) {
        //调用服务层方法
        UserInfo info = this.userService.login(userInfo);
        HashMap<String, Object> map = new HashMap<>();
        if (info != null) {
            //制作token
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            // 制作redisKey
            String key = RedisConst.USER_LOGIN_KEY_PREFIX + token;
            // 放入缓存
            JSONObject userJson = new JSONObject();
            userJson.put("userId", info.getId().toString());
            userJson.put("ip", IpUtil.getIpAddress(request));
            redisTemplate.opsForValue().set(key, userJson.toString(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            //放入数据
            map.put("token", token);
            map.put("nickName", info.getNickName());
            //返回数据
            return Result.ok(map);
        } else {
            //如果为空返回错误信息
            return Result.fail().message("干哈呢!!!密码都能输错");
        }
    }

    /**
     * 退出登录
     *
     * @param
     * @return
     */
    @GetMapping("logout")
    public Result logout(@RequestHeader String token) {
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX + token);
        return Result.ok();
    }
}
