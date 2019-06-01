package com.pinyougou.search.service.impl;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage textMessage= (ObjectMessage) message;
        try {
            Long[] longs = (Long[]) textMessage.getObject();
            //将传来的数据转换为集合
            //删除索引库
            itemSearchService.deleteByGoodsIds(Arrays.asList(longs));
            System.out.println("删除索引");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
