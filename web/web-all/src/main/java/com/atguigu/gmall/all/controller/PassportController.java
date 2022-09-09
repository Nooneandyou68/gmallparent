package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/8 14:42
 */
@Controller
public class PassportController {

    /**
     * 登录跳转功能
     *
     * @return
     */
    @GetMapping("login.html")
    public String login(HttpServletRequest request) {
        request.setAttribute("originUrl", request.getParameter("originUrl"));
        return "login";
    }

}
