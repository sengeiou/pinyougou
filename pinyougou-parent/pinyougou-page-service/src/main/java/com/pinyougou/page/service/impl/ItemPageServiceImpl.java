package com.pinyougou.page.service.impl;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Value("${pagedir}")
    private String pagedir;
    @Autowired
    private TbGoodsMapper tbGoodsMapper;
    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        try {
            //模板名称
            Template template = configuration.getTemplate("item.ftl");
            //创建数据模型
            Map map=new HashMap();
            //根据ID查商品
            TbGoods tbGoods = tbGoodsMapper.selectByPrimaryKey(goodsId);
            map.put("goods",tbGoods);
            //商品扩展数据
            TbGoodsDesc tbGoodsDesc = tbGoodsDescMapper.selectByPrimaryKey(goodsId);
            map.put("goodsDesc",tbGoodsDesc);
            //一级分类
            String name = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
            map.put("itemCat1",name);
            //二级分类
            String name2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
            map.put("itemCat2",name2);
            //三级分类
            String name3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();
            map.put("itemCat3",name3);
            //SKU列表
            TbItemExample example=new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            criteria.andStatusEqualTo("1");//状态有效
            example.setOrderByClause("is_default desc");//排序查询
            List<TbItem> tbItems = itemMapper.selectByExample(example);
            map.put("itemList",tbItems);
            //输出的位置
            FileWriter writer = new FileWriter(pagedir + goodsId + ".html");
            //结合,数据+输出位置
            template.process(map,writer);
            //关闭资源
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for(Long goodsId:goodsIds){
                new File(pagedir+goodsId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
