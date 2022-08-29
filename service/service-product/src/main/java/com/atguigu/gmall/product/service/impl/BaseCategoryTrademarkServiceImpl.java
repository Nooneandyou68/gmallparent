package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategoryTrademark;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.CategoryTrademarkVo;
import com.atguigu.gmall.product.mapper.BaseCategoryTrademarkMapper;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseCategoryTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/28 16:50
 */
@Service
public class BaseCategoryTrademarkServiceImpl extends ServiceImpl<BaseCategoryTrademarkMapper, BaseCategoryTrademark> implements BaseCategoryTrademarkService {
    @Autowired
    private BaseCategoryTrademarkMapper baseCategoryTrademarkMapper;
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    /**
     * 查询分类品牌列表
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 16:56
     */
    @Override
    public List<BaseTrademark> findTrademarkList(Long category3Id) {
        QueryWrapper<BaseCategoryTrademark> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id", category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            List<Long> ids = baseCategoryTrademarkList.stream().map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());
            return baseTrademarkMapper.selectBatchIds(ids);
        }
        return null;
    }

    /**
     * 删除品牌关联
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 23:11
     */
    @Override
    public void removeBaseCategoryTrademarkById(Long category3Id, Long trademarkId) {
        QueryWrapper<BaseCategoryTrademark> categoryTrademarkQueryWrapper = new QueryWrapper<>();
        categoryTrademarkQueryWrapper.eq("category3_id", category3Id);
        categoryTrademarkQueryWrapper.eq("trademark_id", trademarkId);
        baseCategoryTrademarkMapper.delete(categoryTrademarkQueryWrapper);
    }

    /**
     * 获取可选品牌列表
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/28 23:04
     */
    @Override
    public List<BaseTrademark> findCurrentTrademarkList(Long category3Id) {
        QueryWrapper<BaseCategoryTrademark> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id", category3Id);
        List<BaseCategoryTrademark> baseCategoryTrademarkList = baseCategoryTrademarkMapper.selectList(wrapper);
        if (!CollectionUtils.isEmpty(baseCategoryTrademarkList)) {
            //获取到已经绑定的id集合
            List<Long> ids = baseCategoryTrademarkList.stream().map(BaseCategoryTrademark::getTrademarkId).collect(Collectors.toList());
            //拿到所有品牌数据
            List<BaseTrademark> baseTrademarkList = baseTrademarkMapper.selectList(null)
                    .stream().filter(baseTrademark -> !ids.contains(baseTrademark.getId())).
                    collect(Collectors.toList());
            return baseTrademarkList;
        }
        return baseTrademarkMapper.selectList(null);
    }

    /**
     * 保存分类品牌关联
     *
     * @param categoryTrademarkVo
     * @return void
     * @author SongBoHao
     * @date 2022/8/29 16:29
     */
    @Override
    public void save(CategoryTrademarkVo categoryTrademarkVo) {
        //获取要绑定的tmId
        List<Long> trademarkIdList = categoryTrademarkVo.getTrademarkIdList();
        if (!CollectionUtils.isEmpty(trademarkIdList)) {
            List<BaseCategoryTrademark> trademarkList = trademarkIdList.stream().map(tmId -> {
                BaseCategoryTrademark baseCategoryTrademark = new BaseCategoryTrademark();
                baseCategoryTrademark.setCategory3Id(categoryTrademarkVo.getCategory3Id());
                baseCategoryTrademark.setTrademarkId(tmId);
                return baseCategoryTrademark;
            }).collect(Collectors.toList());
            this.saveBatch(trademarkList);
        }
    }
}
