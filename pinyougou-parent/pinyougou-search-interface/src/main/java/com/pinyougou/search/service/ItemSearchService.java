package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 搜索查询
     * @param sarchMap
     * @return
     */
    Map search(Map sarchMap);

    /**
     * 导入数据
     * @param list
     */
    void importList(List list);

    /**
     * 删除商品数据
     * @param goodsIdList
     */
    void deleteByGoodsIds(List goodsIdList);
}
