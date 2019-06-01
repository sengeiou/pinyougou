package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.entity.PageResult;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private TbBrandMapper mapper;

    @Override
    public List<TbBrand> findAll() {
        return mapper.selectByExample(null);
    }

    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Page page= (Page) mapper.selectByExample(null);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public PageResult findPage(TbBrand brand, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);

        TbBrandExample tbBrandExample = new TbBrandExample();
        if(brand!=null){
            //条件查询
            TbBrandExample.Criteria criteria = tbBrandExample.createCriteria();
            if(brand.getName()!=null&&!"".equals(brand.getName())){
                criteria.andNameLike("%"+brand.getName()+"%");
            }
            if(brand.getFirstChar()!=null&&!"".equals(brand.getFirstChar())){
                criteria.andFirstCharEqualTo(brand.getFirstChar());
            }
        }

        Page page= (Page) mapper.selectByExample(tbBrandExample);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void add(TbBrand brand) {
        mapper.insert(brand);
    }

    @Override
    public TbBrand findOne(Long id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    public void update(TbBrand brand) {
        mapper.updateByPrimaryKey(brand);
    }


    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            mapper.deleteByPrimaryKey(id);
        }

    }

    @Override
    public List<Map> selectOptionList() {
        return mapper.selectOptionList();
    }
}
