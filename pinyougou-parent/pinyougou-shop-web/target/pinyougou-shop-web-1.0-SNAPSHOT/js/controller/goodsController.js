//控制层
app.controller('goodsController', function ($scope, $location, $controller, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承


    //状态显示优化
    $scope.status = ['未审核', '已审核', '审核未通过', '关闭'];
    $scope.marketable = ['已下架', '已上架'];
    //1,2,3级显示优化
    $scope.itemCatList = [];
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(function (response) {
            for (var i = 0; i < response.length; i++) {
                $scope.itemCatList[response[i].id] = response[i].name;
            }
        })
    }


    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function () {
        //获取id回显数据
        var id = $location.search()["id"];
        if (id == null) {
            return;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                editor.html($scope.entity.goodsDesc.introduction);//富文本编辑器
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);//图片列表
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);//扩展属性
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);//规格选择
                $scope.entity.itemList = $scope.entity.items;//规格对象
                //规格对象前面的名字
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }

            }
        );
    }
    //规格勾选回显
    $scope.checkAttributeValue = function (specName, optionName) {

        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,
            "attributeName", specName);

        if (object == null) {
            return false;
        } else {
            return object.attributeValue.indexOf(optionName) >= 0;
        }
    }

    //保存
    $scope.save = function () {
        var object;
        if ($scope.entity.goods.id == null) {
            object = goodsService.add($scope.entity);
        } else {
            object = goodsService.update($scope.entity);
        }
        //富文本编辑器内容
        $scope.entity.goodsDesc.introduction = editor.html();
        object.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    alert("保存成功");
                    //成功后跳转页面
                    location.href = "goods.html"
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //上传文件
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(function (response) {
            if (response.success) {
                $scope.image_entity.url = response.message;
            } else {
                alert(response.message)
            }
        })
    }

    //图片列表
    $scope.entity = {goodsDesc: {itemImages: [], specificationItems: []}};
    $scope.add_image_entity = function () {
        if ($scope.image_entity.url == null) {
            alert("未上传图片");
            return;
        }
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
        //保存过后将内容清空,避免回显
        $scope.image_entity = {};
    }

    //移出图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //查询一级下拉列表
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(function (response) {
            $scope.itemCat1List = response;
        })
    }

    //查询二级下拉列表
    $scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat2List = response;
        })
    })

    //查询三级下拉列表
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat3List = response;
        })
    })

    //查询三级分类关联的模板ID
    $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.goods.typeTemplateId = response.typeId;
        })
    })

    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(function (response) {
            $scope.typeTemplate = response;//获取类型模板
            //根据类型模板获取品牌列表
            $scope.typeTemplate.brandIds = JSON.parse(response.brandIds);
            if ($location.search()['id'] == null) {
                //扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
            }
        })
        //规格列表
        typeTemplateService.findSpecList(newValue).success(function (response) {
            $scope.specList = response;
        })
    })

    //勾选的规格选项列表
    $scope.updateSpecAttribute = function ($event, name, value) {
        //从集合中按key找对象   [{“attributeName”:”规格名称”,”attributeValue”:[“规格选项 1”,“规格选项 2”.... ] } , .... ]
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", name);
        //不是第一次添加,只需要向集合中已有的元素后添加即可
        if (object != null) {
            //用户勾选状态
            if ($event.target.checked) {
                object.attributeValue.push(value)
            } else {
                //取消勾选,移出元素
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
                //如果元素移空了,就将对象从总集合中删除
                if (object.attributeValue.length === 0) {
                    $scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
            //第一次添加,向集合中添加完整格式
        } else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]})
        }
    }

    //sku列表
    $scope.createItemList = function () {
        //初始列表
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: "0", isDefault: "0"}];
        //方便操作,封装一下集合
        var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList = addColum($scope.entity.itemList, items[i].attributeName, items[i].attributeValue)
        }

    }

    //封装上面方法的里循环,避免看的复杂 参数1:集合,参数2:规格名称,参数3:规格名称下的集合
    //每次都会创建新的集合,这个新的集合还包含上一次集合
    addColum = function (list, columnName, columnValues) {
        var newList = [];

        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < columnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));
                newRow.spec[columnName] = columnValues[j];
                newList.push(newRow);
            }

        }

        return newList;
    }

    //商品下架上架
    $scope.updateMarketable = function (status) {
        for (var i = 0; i < $scope.selectIds.length; i++) {
            if ($scope.isMarketableList[i] != 1) {
                $scope.isMarketableList=[];
                $scope.selectIds=[];
                $scope.reloadList();
                alert("勾选的商品必须审核通过!");
                return;
            }
        }
        goodsService.updateMarketable($scope.selectIds, status).success(function (response) {
            if (response.success) {
                $scope.isMarketableList=[];
                $scope.selectIds=[];
                $scope.reloadList();
                alert("更改成功");
            } else {
                alert(response.message)
            }
        })
    }

    //以下两个是将勾选值添加到集合中改造为将审核状态也添加到集合中
    $scope.isMarketableList = [];
    //将勾选到的值添加到集合中
    $scope.updateSelectIds = function ($event, id, auditStatus) {
        //$event.target获取input复选框
        if ($event.target.checked) {
            $scope.selectIds.push(id);
            $scope.isMarketableList.push(auditStatus);
        } else {
            //查找被取消勾选的id值的位置
            var index = $scope.selectIds.indexOf(id);
            //从集合中移出元素(元素位置,移出个数)
            $scope.selectIds.splice(index, 1);
            $scope.isMarketableList.splice(index, 1);
        }
    };


});	
