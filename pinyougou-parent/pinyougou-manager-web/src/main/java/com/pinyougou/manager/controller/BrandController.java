package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.Result;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {

    @Reference
    private BrandService service;

    /**
     * 查询所有
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbBrand> findAll(){
        return service.findAll();
    }

    /**
     * 分页查询
     * @param pageNum   当前页
     * @param pageSize  每页记录数
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(Integer pageNum,Integer pageSize){
        return service.findPage(pageNum, pageSize);
    }

    /**
     * 条件分页查询
     * @param brand
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand brand,Integer pageNum,Integer pageSize){
        return service.findPage(brand,pageNum, pageSize);
    }

    /**
     * 添加
     * @param brand
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            service.add(brand);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            return new Result(false,"添加失败");
        }
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        return service.findOne(id);
    }

    /**
     * 修改
     * @param brand
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            service.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            return new Result(false,"修改失败");
        }
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            service.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            return new Result(false,"删除失败");
        }
    }


    /**
     * 查询下拉select2列表数据
     * @return
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return service.selectOptionList();
    }

}
