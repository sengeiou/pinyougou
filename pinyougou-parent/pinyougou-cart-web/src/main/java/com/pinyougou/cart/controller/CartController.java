package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.entity.Result;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference(timeout=6000)
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 从cookie中取出购物车
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
            String cartList = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            //如果购物车为空,给他初始化值
            if ("".equals(cartList) || cartList == null) {
                cartList = "[]";
            }
            List<Cart> carts = JSON.parseArray(cartList, Cart.class);

        if (name.equals("anonymousUser")) {
            System.out.println("cookie...qu");
            return carts;
        } else {
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(name);
            System.out.println("redis...qu");
            if(carts.size()>0){
                cartListFromRedis = cartService.mergeCartList(cartListFromRedis, carts);
                //清除本地 cookie 的数据
                util.CookieUtil.deleteCookie(request, response, "cartList");
                //将合并后的数据存入 redis
                cartService.saveCartListToRedis(name, cartListFromRedis);
            }
            return cartListFromRedis;
        }
    }

    /**
     * 操作购物车
     *
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<Cart> cartList = findCartList();
            List<Cart> list = cartService.addGoodsToCartList(cartList, itemId, num);
            if (name.equals("anonymousUser")) {
                //将操作后的购物车放入cookie中,cookieMaxage cookie存在时间
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(list), 3600 * 24, "UTF-8");
                System.out.println("cookie...cun");
            } else {
                cartService.saveCartListToRedis(name,list);
                System.out.println("redis...cun");
            }
            return new Result(true, "添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加购物车失败");
        }


    }

}
