package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/6 23:07
 */
@Controller
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;

    //通过浏览器会将用户输入的检索条件，直接封装到实体类
    @RequestMapping("list.html")
    public String search(SearchParam searchParam, Model model) {
        //调用查询方法
        Result<Map> result = this.listFeignClient.list(searchParam);
        //urlParam表示点击平台属性值之前的url路径
        String urlParam = this.makeUrlParam(searchParam);
        // 品牌面包屑
        String trademarkParam = this.makeTrademarkParam(searchParam.getTrademark());
        //平台属性面包屑
        List<SearchAttr> propsParamList = this.makePropsParam(searchParam.getProps());
        //排序
        Map<String, Object> orderMap = this.makeOrderMap(searchParam.getOrder());
        //添加到请求域
        model.addAllAttributes(result.getData());
        model.addAttribute("urlParam", urlParam);
        model.addAttribute("searchParam", searchParam);
        model.addAttribute("trademarkParam",trademarkParam );
        model.addAttribute("propsParamList", propsParamList);
        model.addAttribute("orderMap", orderMap);
        //跳转页面
        return "list/index";
    }

    /**
     * 制作排序
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/9/7 22:07
     */
    private Map<String, Object> makeOrderMap(String order) {
        HashMap<String, Object> map = new HashMap<>();
        //判断用户是否点击了排序
        if (!StringUtils.isEmpty(order)) {
            //分割字符
            String[] split = order.split(":");
            if (split != null && split.length == 2) {
                //放入前端所需要的key
                map.put("type", split[0]);
                map.put("sort", split[1]);
            }
        } else {
            //设置默认排序
            map.put("type", 1);
            map.put("sort", "desc");
        }
        return map;
    }

    /**
     * 平台属性面包屑
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/9/7 22:07
     */
    private List<SearchAttr> makePropsParam(String[] props) {
        if (props != null && props.length > 0) {
            List<SearchAttr> searchAttrList = Arrays.asList(props).stream().map(prop -> {
                //分割
                String[] split = prop.split(":");
                //声明一个对象
                SearchAttr searchAttr = new SearchAttr();
                if (split != null && split.length == 3) {
                    searchAttr.setAttrId(Long.parseLong(split[0]));
                    searchAttr.setAttrName(split[2]);
                    searchAttr.setAttrValue(split[1]);
                }
                return searchAttr;
            }).collect(Collectors.toList());
            //返回对象
            return searchAttrList;
        }
        //返回对象
        return null;
    }

    /**
     * 品牌面包屑
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/9/7 22:07
     */
    private String makeTrademarkParam(String trademark) {
        if (!StringUtils.isEmpty(trademark)){
            String[] split = trademark.split(":");
            if (split != null && split.length == 2) {
                return "品牌:" + split[1];
            }
        }
        return null;
    }

    /**
     * 记录用户通过那些条件进行检索
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/9/7 22:07
     */
    private String makeUrlParam(SearchParam searchParam) {
        //字符串拼接
        StringBuilder stringBuilder = new StringBuilder();
        //判断用户根据什么条件进行检索
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            stringBuilder.append("keyword=").append(searchParam.getKeyword());
        }
        //判断用户是否根据分类id检索
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())) {
            stringBuilder.append("category3Id=").append(searchParam.getCategory3Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())) {
            stringBuilder.append("category2Id=").append(searchParam.getCategory2Id());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())) {
            stringBuilder.append("category1Id=").append(searchParam.getCategory1Id());
        }
        //判断用户是否根据品牌检索
        if (!StringUtils.isEmpty(searchParam.getTrademark())) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&trademark=").append(searchParam.getTrademark());
            }
        }
        //判断用户是否根据平台属性值检索
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append("&props=").append(prop);
                }
            }
        }
        //返回用户检索数据
        return "list.html?" + stringBuilder.toString();
    }
}
