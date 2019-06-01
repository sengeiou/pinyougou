package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //查询商品信息,商品信息中包含商家ID
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        String sellerId = item.getSellerId();
        //因为商品页面是静态的,有可能用户进了商品页面之后,商品下架了或者卖完了此时页面在没有删除之前被用户访问到了,所以需要以下两个判断.否则会出现商品下架还被卖出去的结果
        if(item==null){
            throw new RuntimeException("商品不存在");
        }
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态无效");
        }
        //判断购物车中是否有此商家
        Cart cart = searchCartBySellerId(cartList, sellerId);
        //没有就添加商家,并且将商品添加到商家下面
        if(cart==null){
            cart=new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            List<TbOrderItem> list=new ArrayList<>();
            //创建商品明细,然后添加到商家购购物车下
            TbOrderItem orderItem = createOrderItem(item, num);
            list.add(orderItem);
            cart.setOrderItemList(list);
            //添加商家商品
            cartList.add(cart);
        }else {
            //有就商家下面判断是否有此商品
            TbOrderItem tbOrderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if(tbOrderItem==null){
                //没有此商品,将商品添加进去
                tbOrderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(tbOrderItem);
            }else {
                //有此商品,增加数量,修改金额
                tbOrderItem.setNum(tbOrderItem.getNum()+num);
                //有可能是减去商品,当商品数量为0时,从商家集合中删除商家商品
                if(tbOrderItem.getNum()<=0){
                    cart.getOrderItemList().remove(tbOrderItem);
                }
                //当商家中的商品全部删完后,从购物车列表中删除商家
                if(cart.getOrderItemList().size()<=0){
                    cartList.remove(cart);
                }
                tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getPrice().doubleValue()*tbOrderItem.getNum()));
            }

        }


        return cartList;
    }


    /**
     * 判断购物车中是否有商家
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 判断商家下是否有此商品
     * @param list
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> list,Long itemId){
        for (TbOrderItem tbOrderItem : list) {
            if(tbOrderItem.getItemId().longValue()==itemId.longValue()){
                return tbOrderItem;
            }
        }
        return null;
    }

    /**
     * 创建购物车明细
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        TbOrderItem tbOrderItem = new TbOrderItem();
        tbOrderItem.setGoodsId(item.getGoodsId());
        tbOrderItem.setItemId(item.getId());
        tbOrderItem.setNum(num);
        tbOrderItem.setPicPath(item.getImage());
        tbOrderItem.setPrice(item.getPrice());
        tbOrderItem.setSellerId(item.getSellerId());
        tbOrderItem.setTitle(item.getTitle());
        tbOrderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return tbOrderItem;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        //如果redis中购物车为空,返回一个空list
        if(cartList==null||cartList.size()==0){
            return new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList1) {
            for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
                cartList2 = addGoodsToCartList(cartList2, tbOrderItem.getItemId(), tbOrderItem.getNum());
            }
        }
        return cartList2;
    }
}
