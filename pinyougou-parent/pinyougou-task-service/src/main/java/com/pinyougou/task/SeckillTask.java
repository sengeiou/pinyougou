package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    //每分钟执行一次
    @Scheduled(cron = "0 * * * * ?")
    public void refreshSeckillGoods() {
        //取得所有的秒杀商品key,转换为list
        ArrayList seckillGoods = new ArrayList<>(redisTemplate.boundHashOps("seckillGoods").keys());
        //查询正在秒杀的商品列表
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//审核通过
        criteria.andStockCountGreaterThan(0);//剩余库存大于 0
        criteria.andStartTimeLessThanOrEqualTo(new Date());//开始时间小于等于当前时间
        criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间

        //如果缓存中有数据,就排除缓存中的数据,如果没有数据,就将所有商品导入缓存
        if (seckillGoods.size() > 0) {
            criteria.andIdNotIn(seckillGoods);//排除缓存中已经有的商品
        }

        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        //装入缓存
        for (TbSeckillGoods seckill : seckillGoodsList) {
            redisTemplate.boundHashOps("seckillGoods").put(seckill.getId(),
                    seckill);
        }
        System.out.println("将" + seckillGoodsList.size() + "条商品装入缓存");

    }

    /**
     * 移除秒杀商品
     */
    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods() {
        System.out.println("移除秒杀商品任务在执行");
        //扫描缓存中秒杀商品列表，发现过期的移除
        List<TbSeckillGoods> seckillGoodsList =
                redisTemplate.boundHashOps("seckillGoods").values();
        for (TbSeckillGoods seckill : seckillGoodsList) {
            if (seckill.getEndTime().getTime() < new Date().getTime()) {//如果结束日期小于当前日期，则表示过期
                seckillGoodsMapper.updateByPrimaryKey(seckill);//向数据库保存记录
                redisTemplate.boundHashOps("seckillGoods").delete(seckill.getId());//移除缓存数
                System.out.println("移除秒杀商品" + seckill.getId());
            }
        }
        System.out.println("移除秒杀商品任务结束");
    }
}
