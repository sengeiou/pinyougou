app.controller("itemController", function ($scope,$http) {

    //数字+-
    $scope.addNum = function (x) {
        $scope.num = $scope.num + x;
        if ($scope.num < 1) {
            $scope.num = 1;
        }
    }

    $scope.specificationItems = {};//记录用户选择的规格
    $scope.selectSpecification = function (key, value) {
        $scope.specificationItems[key] = value;
        searchSku();
    }

    //判断用户是否选中规格
    $scope.isSelected = function (key, value) {
        if ($scope.specificationItems[key] == value) {
            return true;
        } else {
            return false;
        }
    }

    //动态读取到的sku
    $scope.sku = {};
    $scope.loadSku = function () {
        $scope.sku = skuList[0];
        //深克隆
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));
    }

    //比较map值是否完全相同
    matchObject = function (map1, map2) {
        for (var key in map1) {
            if (map1[key] != map2[key]) {
                return false;
            }
        }
        for (var key in map2) {
            if (map1[key] != map2[key]) {
                return false;
            }
        }
        return true;

    }

    //商品规格选择
    searchSku=function () {
        for(var i=0;i<skuList.length;i++){
            if(matchObject(skuList[i].spec,$scope.specificationItems)){
                $scope.sku=skuList[i];
                return;
            }
        }
        $scope.sku={id:0,title:"???",price:0}
    }

    //添加商品到购物车
    $scope.addToCart=function(){
        $http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
            + $scope.sku.id +'&num='+$scope.num,{'withCredentials':true}).success(
            function(response){
                if(response.success){
                    location.href='http://localhost:9107/cart.html';//跳转到购物车页面
                }else{
                    alert(response.message);
                }
            }
        );
    }

});