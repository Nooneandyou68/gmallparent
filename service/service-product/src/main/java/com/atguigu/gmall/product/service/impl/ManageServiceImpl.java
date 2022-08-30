package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.util.List;

/**
 * @PROJECT_NAME: gamallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/26 10:23
 */
@Service
public class ManageServiceImpl implements ManageService {

    //接口上有@Mapper注解 ，extends  BaseMapper<BaseCategory1> 封装了对表的crud
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    /**
     * 根据一级id查询二级分类数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 10:36
     */
    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    /**
     * 根据二级id查询二级分类数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 10:36
     */
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        return baseCategory2Mapper.selectList(new QueryWrapper<BaseCategory2>().eq("category1_id", category1Id));
    }

    /**
     * 根据三级id查询三级分类数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 10:36
     */
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>().eq("category2_id", category2Id));
    }

    /**
     * 根据分类Id 获取平台属性数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 18:02
     */
    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        return baseAttrInfoMapper.getAttrInfoList(category1Id, category2Id, category3Id);
    }

    /**
     * 保存平台属性方法
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 19:03
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() != null) {//如果不为null代表要执行修改
            baseAttrInfoMapper.updateById(baseAttrInfo);
            // baseAttrValue 平台属性值
            // 修改：通过先删除{baseAttrValue}，在新增的方式！
            // 删除条件：baseAttrValue.attrId = baseAttrInfo.id
            QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
            wrapper.eq("attr_id", baseAttrInfo.getId());
            baseAttrValueMapper.delete(wrapper);
        } else {//如果为null代表要执行添加
            //  新增
            //  保存数据： base_attr_info
            baseAttrInfoMapper.insert(baseAttrInfo);
        }
        //循环遍历添加数据
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (!CollectionUtils.isEmpty(attrValueList)) {
            attrValueList.forEach(baseAttrValue -> {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            });
        }
    }

    /**
     * 根据属性id获取属性值
     *
     * @param attrId
     * @return
     */
    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        QueryWrapper<BaseAttrValue> wrapper = new QueryWrapper<>();
        wrapper.eq("attr_id", attrId);
        return baseAttrValueMapper.selectList(wrapper);
    }

    /**
     * 根据attrId 查询平台属性对象
     *
     * @param attrId
     * @return
     */
    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        //根据attrId查询平台属性
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        if (baseAttrInfo != null) {
            //  查询平台属性值集合数据并赋值
            baseAttrInfo.setAttrValueList(this.getAttrValueList(attrId));
        }
        return baseAttrInfo;
    }

    /**
     * 查询销售属性集合
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/29 17:19
     */
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    /**
     * 根据spuId 查询spuImageList
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> imageQueryWrapper = new QueryWrapper<>();
        imageQueryWrapper.eq("spu_id", spuId);
        return spuImageMapper.selectList(imageQueryWrapper);
    }

    /**
     * 根据spuId 查询销售属性集合
     *
     * @param spuId
     * @return
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }
    /**
     * 保存sku
     *
     * @param skuInfo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        //sku_info
        skuInfoMapper.insert(skuInfo);
        //sku_image
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)) {
            skuImageList.forEach(skuImage -> {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            });
        }
        //sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)) {
            skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            });
        }
        //sku_attr_value
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)) {
            skuAttrValueList.forEach(skuAttrValue -> {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            });
        }

    }

    /**
     * 根据三级分类数据获取到skuInfo 列表
     *
     * @param skuInfoPage
     * @param skuInfo
     * @return
     */
    @Override
    public IPage<SkuInfo> getSpuInfoList(Page<SkuInfo> skuInfoPage, SkuInfo skuInfo) {
        QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
        skuInfoQueryWrapper.eq("category3_id", skuInfo.getCategory3Id());
        return skuInfoMapper.selectPage(skuInfoPage, skuInfoQueryWrapper);
    }

    /**
     * 商品上架
     *
     * @param skuId
     * @return
     */
    @Override
    public void onSale(Long skuId) {
        // 更改销售状态
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }
    /**
     * 商品下架
     *
     * @param skuId
     * @return
     */
    @Override
    public void cancelSale(Long skuId) {
        // 更改销售状态
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }
}
