app.controller("seckillGoodsController", function (seckillGoodsService, $scope, $location, $interval) {
    $scope.findList = function () {
        seckillGoodsService.findList().success(function (response) {
            $scope.list = response;
        })
    }

    //查询秒杀商品详情
    $scope.findOne = function () {
        seckillGoodsService.findOne($location.search()['id']).success(
            function (response) {
                $scope.entity = response;
                //倒计时
                allsecond = Math.floor((new Date($scope.entity.endTime).getTime() - new Date().getTime()) / 1000);//秒

                time = $interval(function () {
                    if (allsecond > 0) {
                        allsecond = allsecond - 1;
                        $scope.timeString = convertTimeString(allsecond);//转换时间字符串
                    } else {
                        $interval.cancel(time);
                        alert("秒杀服务已结束");
                    }
                }, 1000);
            }
        );
    }
    //将秒转换为天-小时-分钟-秒
    convertTimeString = function (allsecond) {
        var days = Math.floor(allsecond / 60 / 60 / 24);//天数
        var hours = Math.floor((allsecond - days * 60 * 60 * 24) / 60 / 60);//小时
        var minutes = Math.floor((allsecond - days * 60 * 60 * 24 - hours * 60 * 60) / 60);//分钟
        var seconds = allsecond - days * 60 * 60 * 24 - hours * 60 * 60 - minutes * 60;//秒数
        var timeString = "";
        if (days > 0) {
            timeString = days + "天 ";
        }
        return timeString + hours + "小时" + minutes + "分钟" + seconds + "秒";
    }
    //提交订单
    $scope.submitOrder=function(){
        seckillGoodsService.submitOrder($scope.entity.id).success(function (response) {
            if(response.success){
                alert("下单成功，请在 1 分钟内完成支付");
                location.href="pay.html";
            }else{
                alert(response.message);
            }
        })
    }
})