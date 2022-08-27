package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/27 18:39
 */
@Service
public class BaseTrademarkServiceImpl implements BaseTrademarkService {

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    /**
     * 品牌分页列表
     *
     * @param
     * @return
     */
    @Override
    public IPage<BaseTrademark> getBaseTrademarkList(Page<BaseTrademark> trademarkPage) {
        //创建筛选对象
        QueryWrapper<BaseTrademark> wrapper = new QueryWrapper<>();
        //设置排序规则
        wrapper.orderByDesc("id");
        //返回数据
        return baseTrademarkMapper.selectPage(trademarkPage, wrapper);
    }
}
