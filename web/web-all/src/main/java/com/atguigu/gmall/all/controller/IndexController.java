package com.atguigu.gmall.all.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/4 23:43
 */
@Controller
public class IndexController {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private TemplateEngine templateEngine;

    @GetMapping({"index.html", "/"})
    public String index(Model model) {
        //远程调用接口获取数据
        Result<List<JSONObject>> result = productFeignClient.getBaseCategoryList();
        List<JSONObject> list = result.getData();
        //添加到请求域
        model.addAttribute("list", list);
        //跳转页面
        return "index/index";
    }

    @GetMapping("createIndex")
    @ResponseBody
    public Result createIndex() {
        //获取后台存储数据
        Result<List<JSONObject>> result = productFeignClient.getBaseCategoryList();
        //设置模板显示内容
        Context context = new Context();
        context.setVariable("list", result.getData());
        //定义文件输入位置
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("D:\\index.html");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //调用process 方法创建模板
        templateEngine.process("index/index.html", context, fileWriter);
        return Result.ok();
    }
}
