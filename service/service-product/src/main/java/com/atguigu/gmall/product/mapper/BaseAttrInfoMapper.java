package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/26 17:07
 */
@Repository
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {
    //根据分类Id 获取平台属性数据
    List<BaseAttrInfo> getAttrInfoList(
            @Param("category1Id") Long category1Id,
            @Param("category2Id")Long category2Id,
            @Param("category3Id")Long category3Id);

    /**
     * 通过skuId 集合来查询数据
     *
     * @param skuId
     * @return
     */
    List<BaseAttrInfo> selectAttrList(Long skuId);
}
