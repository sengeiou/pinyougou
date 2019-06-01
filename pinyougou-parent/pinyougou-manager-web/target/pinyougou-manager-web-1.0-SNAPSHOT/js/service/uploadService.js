app.service("uploadService",function ($http) {

    this.uploadFile=function () {
        //文件上传使用此类
        var formdata=new FormData();
        //参数二:文件上传框的name,取第一个
        formdata.append("file",file.files[0]);
        //返回ajax请求,必须是详细的
        return $http({
            url:"../upload.do",
            method:"post",
            data:formdata,//上传的文件,二进制封装它是一个载体
            headers:{"Content-Type":undefined},//头信息,默认是传json,定义为undefined,就是上传文件
            transformRequest: angular.identity//对表单进行二进制序列化
        })

    }

})