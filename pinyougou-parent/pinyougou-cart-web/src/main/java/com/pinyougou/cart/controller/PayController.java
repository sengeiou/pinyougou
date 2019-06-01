package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.Result;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
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
    private OrderService orderService;
    /**
     * 返回二维码,订单号,支付金额
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //从缓存中读取支付订单还有支付金额
        TbPayLog payLog = orderService.searchPayLogFromRedis(name);
        if(payLog!=null){
            return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }
        return null;
    }

    /**
     * 查看支付状态
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        int x=0;
        while (true){
            Map map = weixinPayService.queryPayStatus(out_trade_no);
            if(map==null){
                return new Result(false,"支付出错");
            }
            if(map.get("trade_state").equals("SUCCESS")){
                //修改订单状态
                orderService.updateOrderStatus(out_trade_no, (String) map.get("transaction_id"));
                return new Result(true,"支付成功");
            }
            try {
                //间隔三秒查询一次
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if(x>=100){
                return new Result(false,"二维码过期");
            }
        }
    }

}
