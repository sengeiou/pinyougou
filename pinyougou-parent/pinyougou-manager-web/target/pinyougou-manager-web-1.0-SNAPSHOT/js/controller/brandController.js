app.controller("brandController", function ($scope, brandService,$controller) {

    //继承共有的controller
    $controller("baseController",{$scope:$scope});

    //查询品牌列表
    $scope.findAll = function () {
        brandService.findAll().success(function (data) {
            $scope.brandList = data;
        });
    };


    //请求分页查询数据
    $scope.findPage = function (pageNum, pageSize) {
        brandService.findPage(pageNum,pageSize).success(function (data) {
            $scope.brandList = data.rows;//当前页记录
            $scope.paginationConf.totalItems = data.total;//总记录数
        })
    };

    //初始化分页条件查询的条件,因为一开始进入页面是没有条件的,没有条件条件为null页面会直接报错
    $scope.searchEntity={};
    //条件分页查询
    $scope.search = function (pageNum, pageSize) {
        brandService.search(pageNum,pageSize,$scope.searchEntity).success(function (data) {
            $scope.brandList = data.rows;//当前页记录
            $scope.paginationConf.totalItems = data.total;//总记录数
        })
    };

    //包含新建和修改
    $scope.save = function () {
        //默认是新建方法
        var methodName = null;
        //如果id不为null,那么就代表是修改
        if ($scope.entity.id != null) {
            methodName = brandService.update($scope.entity);
        }else {
            methodName = brandService.add($scope.entity);
        }
        methodName.success(function (data) {
            if (data.success) {
                //刷新页面数据
                $scope.reloadList();
            } else {
                alert(data.message);
            }
        });
    };

    //根据id查询
    $scope.findOne = function (id) {
        brandService.findOne(id).success(function (data) {
            $scope.entity = data;
        })
    };

    //传入id集合,删除产品
    $scope.dele = function () {
        brandService.dele($scope.selectIds).success(function (data) {
            if (data.success) {
                //刷新页面数据
                $scope.reloadList();
                //删除完成之后将集合清空
                $scope.selectIds = [];
            } else {
                alert(data.message);
            }
        });
    };


});