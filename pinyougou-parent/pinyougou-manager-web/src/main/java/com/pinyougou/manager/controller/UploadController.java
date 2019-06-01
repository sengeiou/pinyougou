package com.pinyougou.manager.controller;

import com.pinyougou.entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file){
        if(file==null){
            return new Result(false,"未添加图片");
        }
        //获取文件名称
        String name = file.getOriginalFilename();
        //文件后缀名,substring截取,lastIndexOf从最后一个.开始算,+1就是不包括.
        String substring = name.substring(name.lastIndexOf(".") + 1);

        try {
            //构建客户端对象
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //三个参数,第一个是文件内容,第二个是文件后缀名,第三个是文件扩展信息,不需要,工具类中直接写成了null
            String fileName = fastDFSClient.uploadFile(file.getBytes(), substring);
            //拼接服务器地址,最终形成图片访问路径
            return new Result(true,FILE_SERVER_URL+fileName);
        } catch (Exception e) {
            return new Result(false,"添加失败");
        }

    }

}
