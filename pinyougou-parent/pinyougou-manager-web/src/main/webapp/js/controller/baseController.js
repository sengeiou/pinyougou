//共有的方法
app.controller("baseController",function ($scope) {

    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,//当前页
        totalItems: 10,//总记录数
        itemsPerPage: 10,//每页记录数
        perPageOptions: [10, 20, 30, 40, 50],//分页选项,每页显示条数
        onChange: function () {//当页码重新变更后自动触发的方法
            $scope.reloadList();
        }
    };

    $scope.reloadList = function () {
        // $scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        // 不用分页查询数据了,改用新的条件分页查询
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };


    //需要删除的id集合
    $scope.selectIds = [];
    //将勾选到的值添加到集合中
    $scope.updateSelection = function ($event, id) {
        //$event.target获取input复选框
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        } else {
            //查找被取消勾选的id值的位置
            var index = $scope.selectIds.indexOf(id);
            //从集合中移出元素(元素位置,移出个数)
            $scope.selectIds.splice(index, 1);
        }
    };

    //json数据优化显示
    $scope.jsonToString=function (jsonString,key) {
        var json = JSON.parse(jsonString);
        var value = "";
        for(var i=0;i<json.length;i++){
            //字符串之间拼接逗号隔开,除了第一个
            if(i>0){
                value+=",";
            }
            value += json[i][key];
        }
        return value;
    }


});