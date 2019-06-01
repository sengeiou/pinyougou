app.controller("loginController",function ($scope, loginService) {

    $scope.getName=function () {
        loginService.loginName().success(function (data) {
            $scope.loginName=data.loginName;
        })
    }

})