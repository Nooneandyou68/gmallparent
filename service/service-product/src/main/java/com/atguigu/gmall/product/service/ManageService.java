package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;

import java.util.List;

/**
 * @PROJECT_NAME: gamallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/8/26 10:22
 */
public interface ManageService {
    /**
     * 查询所有
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 10:27
     */
    List<BaseCategory1> getCategory1();

    /**
     * 根据一级id查询二级分类数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 10:38
     */
    List<BaseCategory2> getCategory2(Long category1Id);

    /**
     * 根据二级id查询三级分类数据
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/8/26 10:36
     */
    List<BaseCategory3> getCategory3(Long category2Id);

}
