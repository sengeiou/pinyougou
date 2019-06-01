package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.Result;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;
    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 返回二维码,订单号,支付金额
     *
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //从缓存中读取支付订单还有支付金额
        TbSeckillOrder tbSeckillOrder = seckillOrderService.searchOrderFromRedisByUserId(name);
        if (tbSeckillOrder != null) {
            return weixinPayService.createNative(tbSeckillOrder.getId() + "", (long) (tbSeckillOrder.getMoney().doubleValue() * 100) + "");
        }
        return null;
    }

    /**
     * 查看支付状态
     *
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        int x = 0;
        while (true) {
            Map map = weixinPayService.queryPayStatus(out_trade_no);
            if (map == null) {
                return new Result(false, "支付出错");
            }
            if (map.get("trade_state").equals("SUCCESS")) {
                //修改订单状态
                seckillOrderService.saveOrderFromRedisToDb(name, new Long(out_trade_no), map.get("transaction_id") + "");
                return new Result(true, "支付成功");
            }
            try {
                //间隔三秒查询一次
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x >= 100) {
                //关闭交易订单
                Map map1 = weixinPayService.closePay(out_trade_no);
                //关闭交易订单的时候,订单可能已经被支付,那么就查询一下
                if (map1 != null && "FAIL".equals(map1.get("return_code"))) {
                    if ("ORDERPAID".equals(map1.get("err_code"))) {
                        //修改订单状态
                        seckillOrderService.saveOrderFromRedisToDb(name, new Long(out_trade_no), map.get("transaction_id") + "");
                        return new Result(true, "支付成功");
                    }
                }
                //删除用户的秒杀订单,并且恢复库存
                seckillOrderService.deleteOrderFromRedis(name, new Long(out_trade_no));
                return new Result(false, "二维码超时");
            }
        }
    }

}
