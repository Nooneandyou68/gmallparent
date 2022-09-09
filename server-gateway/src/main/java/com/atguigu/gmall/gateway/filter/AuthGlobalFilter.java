package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.convert.Bucket;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/8 20:35
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {

    @Autowired
    private RedisTemplate redisTemplate;

    // 匹配路径的工具类
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${authUrls.url}")
    private String authUrls;

    /**
     * 过滤方法
     *@author SongBoHao
     *@date 2022/9/8 20:55
     *@param exchange spring 框架封装的web服务请求 request与响应response对象
     *@param chain 过滤器链对象
     *@return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        //获取请求路径
        String path = request.getURI().getPath();
        //判断是否匹配要拦截的路径
        if (antPathMatcher.match("/**/inner/**", path)) {
            //设置响应
            ServerHttpResponse response = exchange.getResponse();
            //如果是内部接口，则网关拦截不允许外部访问
            return out(response, ResultCodeEnum.PERMISSION);
        }
        //1、先获取登录id，在缓存中获取数据，必须要有token， token存储在header或cookie中
        String userId = this.getUserId(request);
        //2、判断是否属于非法登录
        if ("-1".equals(userId)) {
            //设置响应
            ServerHttpResponse response = exchange.getResponse();
            //提示用户未登录没有权限
            return out(response, ResultCodeEnum.PERMISSION);
        }
        if (antPathMatcher.match("/api/**/auth/**", path)) {
            //判断用户是否登录 如果未登录， 则提示信息
            if (StringUtils.isEmpty(userId)) {
                //提示信息
                ServerHttpResponse response = exchange.getResponse();
                //提示用户未登录
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }
        // 用户在访问那些业务层控制器时，需要登录
        String[] split = authUrls.split(",");
        if (split != null && split.length > 0) {
            for (String uri : split) {
                //path包含上述控制器 list.html 并且用户id为空
                if (path.indexOf(uri) != -1 && StringUtils.isEmpty(userId)) {
                    //获取响应
                    ServerHttpResponse response = exchange.getResponse();
                    //设置参数
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    //重定向到登录页面
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://www.gmall.com/login.html?originUrl=" + request.getURI());
                    //重定向
                    return response.setComplete();
                }
            }
        }
        //将获取到的用户id添加到请求头
        if (!StringUtils.isEmpty(userId)) {
            //放入请求头
            request = request.mutate().header("userId", userId).build();
            // exchange与request关联起来
            return chain.filter(exchange.mutate().request(request).build());
        }
        // 默认返回 表示这个过滤器结束了
        return chain.filter(exchange);
    }

    /**
     * 输出方法
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/9/8 22:04
     */
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        // 构建输出的内容
        Result result = Result.build(null, resultCodeEnum);
        // 将result 变为字符串
        String message = JSON.toJSONString(result);
        // 产生dataBuffer
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(message.getBytes());
        // 设置每个页面的内容类型
        response.getHeaders().set("content-type", "application/json; charset=UTF");
        //输出数据
        return response.writeWith(Mono.just(dataBuffer));
    }

    /**
     * 获取用户id方法
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/9/8 22:01
     */
    private String getUserId(ServerHttpRequest request) {
        // 用户id可能存储在cookie header
        HttpCookie httpCookie = request.getCookies().getFirst("token");
        String token = "";
        if (httpCookie != null) {
            token = httpCookie.getValue();
        } else {
            //如果cookie 中没有 token 在从header中获取
            List<String> stringList = request.getHeaders().get("token");
            if (!CollectionUtils.isEmpty(stringList)) {
                token = stringList.get(0);
            }
        }
        //判断token不为空
        if (!StringUtils.isEmpty(token)) {
            //组成缓存key
            String userLoginKey = "user:login:" + token;
            //从缓存中获取userId
            String redisValue = (String) redisTemplate.opsForValue().get(userLoginKey);
            if (StringUtils.isEmpty(redisValue)) {
                JSONObject jsonObject = JSONObject.parseObject(redisValue);
                //取出ip
                String ip = (String) jsonObject.get("ip");
                //判断当前缓存中ip与正在操作的客户端ip地址是否一致
                if (IpUtil.getGatwayIpAddress(request).equals(ip)) {
                    //如果相同返回用户id
                    String userId = (String) jsonObject.get("userId");
                    return userId;
                } else {
                    return "-1";
                }
            }
        }
        //默认返回空串
        return "";
    }
}
