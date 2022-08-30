package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/30 20:18
 */
@Service
public class SkuInfoServiceImpl implements SkuInfoService {
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    /**
     * 修改sku数据回显
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/30 20:13
     */
    @Override

    public SkuInfo getSkuInfo(Long id) {
        SkuInfo skuInfo = skuInfoMapper.selectById(id);
        List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", id));
        skuInfo.setSkuImageList(skuImageList);
        //查询sku_attr_value
        QueryWrapper<SkuAttrValue> skuAttrValueQueryWrapper = new QueryWrapper<>();
        skuAttrValueQueryWrapper.eq("sku_id", skuInfo.getId());
        List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.selectList(skuAttrValueQueryWrapper);
        skuAttrValues.forEach(skuAttrValue -> {
            QueryWrapper<BaseAttrValue> valueQueryWrapper = new QueryWrapper<>();
            valueQueryWrapper.eq("id", skuAttrValue.getValueId());
            valueQueryWrapper.eq("attr_id", skuAttrValue.getAttrId());
            List<BaseAttrValue> attrValueList = baseAttrValueMapper.selectList(valueQueryWrapper);
            attrValueList.forEach(baseAttrValue -> {
                skuAttrValue.setValueName(baseAttrValue.getValueName());
                QueryWrapper<BaseAttrInfo> attrInfoQueryWrapper = new QueryWrapper<>();
                attrInfoQueryWrapper.eq("id", baseAttrValue.getAttrId());
                List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectList(attrInfoQueryWrapper);
                baseAttrInfoList.forEach(baseAttrInfo -> {
                    skuAttrValue.setAttrName(baseAttrInfo.getAttrName());
                });
            });
        });
        skuInfo.setSkuAttrValueList(skuAttrValues);
        //查询spu_sale_attr_value
        QueryWrapper<SkuSaleAttrValue> skuSaleAttrValueQueryWrapper = new QueryWrapper<>();
        skuSaleAttrValueQueryWrapper.eq("sku_id", skuInfo.getId());
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.selectList(skuSaleAttrValueQueryWrapper);
        skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
            QueryWrapper<SpuSaleAttrValue> valueQueryWrapper = new QueryWrapper<>();
            valueQueryWrapper.eq("id", skuSaleAttrValue.getSaleAttrValueId());
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttrValueMapper.selectList(valueQueryWrapper);
            spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                skuSaleAttrValue.setBaseSaleAttrId(spuSaleAttrValue.getBaseSaleAttrId());
                skuSaleAttrValue.setSaleAttrName(spuSaleAttrValue.getSaleAttrName());
                skuSaleAttrValue.setSaleAttrValueName(spuSaleAttrValue.getSaleAttrValueName());
            });
        });

        skuInfo.setSkuSaleAttrValueList(skuSaleAttrValueList);
        //返回数据
        return skuInfo;
    }

    /**
     * 保存修改sku
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/30 23:48
     */
    @Override
    public void updateSkuInfo(SkuInfo skuInfo) {
        //根据id删除
        QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
        skuInfoQueryWrapper.eq("id", skuInfo.getId());
        skuInfoQueryWrapper.eq("spu_id", skuInfo.getSpuId());
        skuInfoMapper.delete(skuInfoQueryWrapper);

        skuInfoMapper.insert(skuInfo);
    }
}
