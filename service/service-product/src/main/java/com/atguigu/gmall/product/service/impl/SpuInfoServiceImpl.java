package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.aspectj.weaver.Shadow.ExceptionHandler;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/27 18:16
 */
@Service
public class SpuInfoServiceImpl implements SpuInfoService {

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuPosterMapper spuPosterMapper;

    @Override
    public IPage<SpuInfo> getSpuList(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo) {
        //创建筛选对象
        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        //设置筛选条件
        wrapper.eq("category3_id", spuInfo.getCategory3Id());
        //设置排序规则
        wrapper.orderByDesc("id");
        //返回数据
        return spuInfoMapper.selectPage(spuInfoPage, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        spuInfoMapper.insert(spuInfo);
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        spuImageList.forEach(spuImage -> {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insert(spuImage);
        });

        List<SpuPoster> spuPosterList = spuInfo.getSpuPosterList();
        spuPosterList.forEach(spuPoster -> {
            spuPoster.setSpuId(spuInfo.getId());
            spuPosterMapper.insert(spuPoster);
        });

        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        spuSaleAttrList.forEach(spuSaleAttr -> {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insert(spuSaleAttr);
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            });
        });
    }
    /**
     * 修改数据回显
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/30 19:31
     */
    @Override
    public SpuInfo getSpuInfo(Long spuId) {
        SpuInfo spuInfo = spuInfoMapper.selectById(spuId);
        //根据spuId查询spu销售属性集合
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
        spuInfo.setSpuSaleAttrList(spuSaleAttrList);
        //根据spuId查询spu_image集合
        List<SpuImage> spuImageList = spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id", spuId));
        spuInfo.setSpuImageList(spuImageList);
        //根据spuId查询海报集合
        List<SpuPoster> spuPosterList = spuPosterMapper.selectList(new QueryWrapper<SpuPoster>().eq("spu_id", spuId));
        spuInfo.setSpuPosterList(spuPosterList);
        return spuInfo;
    }

    /**
     * 修改保存
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/30 19:31
     */
    @Override
    public void updateSpuInfo(SpuInfo spuInfo) {
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("id", spuInfo.getId());
        spuInfoMapper.delete(spuInfoQueryWrapper);
        spuInfoMapper.insert(spuInfo);
    }
}
