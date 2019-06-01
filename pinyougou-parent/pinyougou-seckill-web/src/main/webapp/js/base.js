//未分页模块
var app = angular.module("pinyougou", []);

//前面是模块名,后面是隐式注入
app.filter('trustHtml',['$sce',function ($sce) {
    //此处传入的参数是被过滤的内容
    return function (data) {
        //返回的是过滤后,信任html
        return $sce.trustAsHtml(data);
    }
}]);