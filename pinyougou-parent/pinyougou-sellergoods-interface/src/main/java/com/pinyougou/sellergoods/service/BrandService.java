package com.pinyougou.sellergoods.service;

import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbBrand;

import java.util.List;
import java.util.Map;

public interface BrandService {

    /**
     * 查询所有
     * @return
     */
    List<TbBrand> findAll();

    /**
     * 分页查询所有
     * @param pageNum   当前页
     * @param pageSize  每页记录数
     * @return
     */
    PageResult findPage(Integer pageNum, Integer pageSize);

    /**
     * 条件分页查询
     * @param brand
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageResult findPage(TbBrand brand,Integer pageNum, Integer pageSize);

    /**
     * 添加
     * @param brand
     */
    void add(TbBrand brand);


    /**
     * 根据ID查询
     * @param id
     * @return
     */
    TbBrand findOne(Long id);

    /**
     * 修改
     * @param brand
     */
    void update(TbBrand brand);

    /**
     * 根据id删除(复式删除)
     * @param ids
     */
    void delete(Long[] ids);

    /**
     * 返回下拉列表数据
     * @return
     */
    List<Map> selectOptionList();

}
