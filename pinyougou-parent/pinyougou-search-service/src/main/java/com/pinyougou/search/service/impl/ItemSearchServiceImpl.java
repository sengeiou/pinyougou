package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {
        //将结果存储到map中返回到前端
        Map map = new HashMap();
        //关键字处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));
        //封装查询高亮列表
        map.putAll(searchList(searchMap));
        //封装分类名称
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);
        //根据商品分类查询品牌和规格数据
        //先提取用户点击的分类
        String category = (String) searchMap.get("category");
        //用户没有点击分类,查询第一个
        if("".equals(category)){
            if(categoryList.size()>0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
            //用户点击了分类,查询用户点击的分类
        }else {
            map.putAll(searchBrandAndSpecList(category));
        }
        return map;
    }


    /**
     * 查询高亮列表,返回结果写成map,方便扩展
     *
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        //2.
        HighlightQuery query = new SimpleHighlightQuery();

        //4.1设置在哪个域上面设置高亮
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //4.2设置前缀,不一定只是设置高亮
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //4.3设置后缀
        highlightOptions.setSimplePostfix("</em>");
        //4.设置高亮
        query.setHighlightOptions(highlightOptions);
        //3.关键字条件查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //防止用户没有商品过滤,商品过滤
        if(!"".equals(searchMap.get("category"))) {
            //2.商品分类过滤
            FilterQuery filteQuery = new SimpleFilterQuery();
            //4.根据哪个域过滤
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            //3.
            filteQuery.addCriteria(filterCriteria);
            //1.
            query.addFilterQuery(filteQuery);
        }
        //防止用户没有品牌过滤,品牌过滤
        if(!"".equals(searchMap.get("brand"))) {
            //2.商品分类过滤
            FilterQuery filteQuery = new SimpleFilterQuery();
            //4.根据哪个域过滤
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            //3.
            filteQuery.addCriteria(filterCriteria);
            //1.
            query.addFilterQuery(filteQuery);
        }
        //规格过滤
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
            Set<Map.Entry<String, String>> entries = specMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                //2.规格过滤
                FilterQuery filteQuery = new SimpleFilterQuery();
                //4.根据哪个域过滤,此处是规格,规格有多个,并且是动态的.所以key要动态获取
                Criteria filterCriteria = new Criteria("item_spec_"+entry.getKey()).is(specMap.get(entry.getKey()));
                //3.
                filteQuery.addCriteria(filterCriteria);
                //1.
                query.addFilterQuery(filteQuery);
            }
        }
        //价格过滤
        String price = (String) searchMap.get("price");
        //传过来价格就进行过滤
        if(!"".equals(price)){
            String[] split = price.split("-");
            //如果最小价格不等于0,那么就取传过来最小的价格
            if(!"0".equals(split[0])){
                //2.商品分类过滤
                FilterQuery filteQuery = new SimpleFilterQuery();
                //4.根据哪个域过滤,greaterThanEqual大于等于.字符串也会自动转换
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(split[0]);
                //3.
                filteQuery.addCriteria(filterCriteria);
                //1.
                query.addFilterQuery(filteQuery);
            }
            //如果最大价格不等于*,就取传过来的最大价格
            if(!"*".equals(split[1])){
                //2.商品分类过滤
                FilterQuery filteQuery = new SimpleFilterQuery();
                //4.根据哪个域过滤,lessThanEqual小于等于.字符串也会自动转换
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(split[1]);
                //3.
                filteQuery.addCriteria(filterCriteria);
                //1.
                query.addFilterQuery(filteQuery);
            }
        }
        //价格排序
        String sortValue= (String) searchMap.get("sort");//ASC DESC
        String sortField= (String) searchMap.get("sortField");//排序字段
        if(!"".equals(sortField)&&!"".equals(sortValue)){
            if(sortValue.equals("ASC")){
                Sort sort=new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if(sortValue.equals("DESC")){
                Sort sort=new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }

        //当前页码
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if("".equals(pageNo)){
            pageNo=1;
        }
        //每页记录数
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if("".equals(pageSize)){
            pageSize=20;
        }
        //从第几页查询,此处是计算公式
        query.setOffset((pageNo-1)*pageSize);
        //设置每页记录数
        query.setRows(pageSize);
        //1.
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //5.获得高亮入口集合(每条记录的入口)
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : entryList) {
            //6.通过入口获得获得高亮列表,也就是设置高亮域的高亮结果集合.目前只设置了一个高亮域所以长度为1,如果设置多个高亮域,这里面存的就是多个.
            List<HighlightEntry.Highlight> highlights = entry.getHighlights();
            //最终的数据集合
            for (HighlightEntry.Highlight highlight : highlights) {
                //因为域中存储的是多条数据,所以这里也是集合.
                List<String> snipplets = highlight.getSnipplets();
                //获取原有数据,然后将高亮结果替换到原有的结果中.
                for (String snipplet : snipplets) {
                    entry.getEntity().setTitle(snipplet);
                }
            }
        }
        map.put("rows", page.getContent());
        map.put("totalPages", page.getTotalPages());//返回总页数
        map.put("total", page.getTotalElements());//返回总记录数
        return map;
    }

    /**
     * 获取分类名称
     *
     * @param searchMap
     * @return
     */
    private List<String> searchCategoryList(Map searchMap) {
        //2.构建条件查询  where
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //4.根据哪个域分组 group by
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        //3.构建分组
        query.setGroupOptions(groupOptions);
        //1.查询,完成上面的步骤,得到分组页对象,分组页对象可能会包含多个分组结果,根据第4步决定
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //5.获取分组结果对象
        GroupResult<TbItem> item_category = page.getGroupResult("item_category");
        //6.获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = item_category.getGroupEntries();
        //7.最终分组结果集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        //因为分组结果会得到多个,所以循环遍历存到list中
        List list = new ArrayList();
        for (GroupEntry<TbItem> entry : content) {
            list.add(entry.getGroupValue());
        }
        return list;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询品牌规格列表
     *
     * @param category
     * @return
     */
    private Map searchBrandAndSpecList(String category) {
        //根据商品分类名称得到模板id
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        Map map = new HashMap();
        //非空判断容错.
        if (templateId != null) {
            //根据模板id获取品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            //根据模板id获取规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);

            map.put("brandList",brandList);
            map.put("specList",specList);
        }
        return map;
    }


    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        //2
        SolrDataQuery query=new SimpleQuery();
        //4,删除的域和条件
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        //3
        query.addCriteria(criteria);
        //1
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
