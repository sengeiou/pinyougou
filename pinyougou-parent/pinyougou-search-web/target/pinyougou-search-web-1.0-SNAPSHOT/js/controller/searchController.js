app.controller("searchController", function ($scope,$location, searchService) {

    $scope.loadkeywords=function(){
        $scope.searchMap.keywords= $location.search()['keywords'];
        //直接去搜索
        $scope.search();
    }
    //初始化定义对象
    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': 1,
        'pageSize': 30,
        'sortField': '',
        'sort': ''
    }
    //搜索
    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;
            buildPageLabel();
        })
    }

    //改变searchMap值,需要两个参数,一个是用户点的是属于哪一类,一个是用户点的具体参数
    $scope.addSearchItem = function (key, value) {
        //用户点击的是品牌或者是分类,key是确定的
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = value;
        } else {
            //用户点击的是规格,规格的key是不确定的
            $scope.searchMap.spec[key] = value;
        }
        //当用户点击分类后立马查询
        $scope.search();
    }

    //删除searchMap值
    $scope.removeSearchItem = function (key) {
        //用户点击的是品牌或者是分类,key是确定的
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchMap[key] = "";
        } else {
            //用户点击的是规格,规格的key是不确定的
            delete $scope.searchMap.spec[key];
        }
        //当用户点击分类后立马查询
        $scope.search();
    }

    buildPageLabel = function () {
        var firstPage = 1;//开始页码
        var lastPage = $scope.resultMap.totalPages;//总页码
        var pageNo = $scope.searchMap.pageNo;//当前页
        //前面的省略号
        $scope.firstDot = true;
        //后面的省略号
        $scope.lastDot = true;
        if (lastPage >= 5) {//如果总页码大于5条
            if (pageNo >= 3) {//如果当前页大于等于3,就代表处于中间,那么只显示前两条和后两条
                firstPage = pageNo - 2;
                lastPage = pageNo + 2;
            } else {//否则代表现在还在小于3条,无法减,那么就只显示前5条
                lastPage = 5;
                $scope.firstDot = false;
            }
            //如果当前页+2大于总页数,代表翻到最后面了,就直接显示最后一页,这里只能用全局变量的总页数
            if (pageNo + 2 >= $scope.resultMap.totalPages) {
                lastPage = $scope.resultMap.totalPages;
                $scope.lastDot = false;
            }
            if (pageNo === lastPage) {//如果当前页等于最后一页,那么就倒数5页
                firstPage = lastPage - 4;
            }
        }

        //分页
        $scope.pageLabel = [];
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }

    //点击分页查询
    $scope.queryByPage = function (pageNo) {
        pageNo = parseInt(pageNo);
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    //排序
    $scope.sortSearch = function (sortField, sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }

    //隐藏品牌
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {
                //如果包含就不显示
                return false;
            }
        }
        return true;
    }


})