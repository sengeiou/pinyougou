//购物车控制层
app.controller('cartController', function ($scope, cartService) {
    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.cartList = response;
                sum();
            }
        );
    }
    //增减购物车
    $scope.addGoodsToCartList = function (itemId, num) {
        cartService.addGoodsToCartList(itemId, num).success(function (response) {
            if (response.success) {
                $scope.findCartList();//刷新列表
            } else {
                alert(response.message);//弹出错误提示
            }
        })
    }
    //计算总商品数量和总价格
    sum = function () {
        $scope.totalNum = 0;//总数量
        $scope.totalMoney = 0;//总金额
        for (var i = 0; i < $scope.cartList.length; i++) {
            var cart = $scope.cartList[i];
            for (var j = 0; j < cart.orderItemList.length; j++) {
                var orderItem = cart.orderItemList[j];
                $scope.totalNum += orderItem.num;
                $scope.totalMoney += orderItem.totalFee;
            }
        }
    }

    //查询地址
    $scope.findAddressList = function () {
        cartService.findAddressList().success(function (response) {
            $scope.addressList = response;
            for (var i = 0; i < $scope.addressList.length; i++) {
                if ($scope.addressList[i].isDefault == '1') {
                    $scope.address = $scope.addressList[i];
                    break;
                }
            }
        })
    }

    //选择地址
    $scope.selectAddress = function (address) {
        $scope.address = address;
    }

    $scope.isSelectedAddress = function (address) {
        if (address === $scope.address) {
            return true;
        } else {
            return false;
        }
    }

    $scope.order = {paymentType: '1'};
    //选择支付方式
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    }

    //保存订单
    $scope.submitOrder = function () {
        $scope.order.receiverAreaName = $scope.address.address;//地址
        $scope.order.receiverMobile = $scope.address.mobile;//手机
        $scope.order.receiver = $scope.address.contact;//联系人
        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.success) {
                    //页面跳转
                    if ($scope.order.paymentType === '1') {//如果是微信支付，跳转到支付页面
                        location.href = "pay.html";
                    } else {//如果货到付款，跳转到提示页面
                        location.href = "paysuccess.html";
                    }
                } else {
                    alert(response.message); //也可以跳转到提示页面
                }
            }
        );
    }

});