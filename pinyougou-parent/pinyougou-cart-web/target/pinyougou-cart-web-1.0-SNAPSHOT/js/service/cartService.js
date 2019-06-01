app.service("cartService",function ($http) {
    //查询购物车列表
    this.findCartList=function () {
        return $http.get("cart/findCartList.do")
    }
    //增减购物车
    this.addGoodsToCartList=function (itemId,num) {
        return $http.get("cart/addGoodsToCartList.do?itemId="+itemId+"&num="+num);
    }
    //查询地址
    this.findAddressList = function () {
        return $http.get("address/findListByUserId.do");
    }
    //保存订单
    this.submitOrder=function(order){
        return $http.post('order/add.do',order);
    }
})