//控制层
app.controller('itemCatController', function ($scope, $controller, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        itemCatService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        itemCatService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        itemCatService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    $scope.findOneTypeTemplate = function (id) {
        typeTemplateService.findOne(id).success(function (data) {
            $scope.entity.typeTemplate = {id: data.id, text: data.name}
        })
    }

    //记录上级ID
    $scope.parentId = 0;
    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            $scope.entity.typeId = $scope.entity.typeTemplate.id;
            serviceObject = itemCatService.update($scope.entity); //修改
        } else {
            $scope.entity.parentId = $scope.parentId;
            $scope.entity.typeId = $scope.entity.typeTemplate.id;
            serviceObject = itemCatService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.findByParentId($scope.parentId);//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        itemCatService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.findByParentId($scope.parentId);//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.findByParentIds = function (selectIds) {
        for (var i = 0; i < $scope.selectIds.length; i++) {
            itemCatService.findByParentId($scope.selectIds[i]).success(function (response) {
                $scope.listLength = response.length;
                if ($scope.listLength !== 0) {
                    $scope.findByParentId($scope.parentId);
                    alert("选中分类下有子分类");
                } else {
                    $scope.dele();
                }
            });
        }
    }


    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        itemCatService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //根据上级ID查询分类
    $scope.findByParentId = function (parentId) {
        itemCatService.findByParentId(parentId).success(function (data) {
            $scope.list = data;
        })
    }

    //面包屑
    $scope.grade = 1;//级别
    $scope.setGrade = function (value) {
        $scope.grade = value;
    }

    $scope.selectList = function (p_entity) {
        if ($scope.grade == 1) {
            $scope.entity_1 = null;
            $scope.entity_2 = null;
        }
        if ($scope.grade == 2) {
            $scope.entity_1 = p_entity;
            $scope.entity_2 = null;
        }
        if ($scope.grade == 3) {
            $scope.entity_2 = p_entity;
        }

        $scope.findByParentId(p_entity.id);
        $scope.parentId = p_entity.id;
        $scope.selectIds = [];
    }

    //下拉列表
    $scope.typeTemplate = {data: []};
    $scope.selectOptionLis = function () {
        typeTemplateService.selectOptionList().success(function (response) {
            $scope.typeTemplate = {data: response}
        })
    }
});
