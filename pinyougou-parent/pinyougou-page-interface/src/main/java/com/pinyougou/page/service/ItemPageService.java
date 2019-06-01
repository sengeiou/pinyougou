package com.pinyougou.page.service;

public interface ItemPageService {

    /**
     * 生成商品详情页
     * @param goodsId
     * @return
     */
    boolean genItemHtml(Long goodsId);

    /**
     * 删除页面
     * @param goodsIds
     * @return
     */
    boolean deleteItemHtml(Long[] goodsIds);

}
