package com.example.controller;

import cn.hutool.core.io.FileUtil;
import com.example.util.Result;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/files")
public class FileController {

    //表示本地磁盘文件的存储路径,D:\IdeaProjects\MusicDreamer
    private static final String filePath = System.getProperty("user.dir") + "/uploadFiles/";

    //文件上传，支持分类存储
    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file,
                        @RequestParam(value = "type", defaultValue = "other") String type) throws IOException {
        String basePath = System.getProperty("user.dir") + "/uploadFiles/";
        String typePath = basePath + type + "/";
        if(!FileUtil.exist(typePath)) {
            FileUtil.mkdir(typePath);
        }
        String originalFileName = file.getOriginalFilename();
        String realFilePath = typePath + originalFileName;
        if (FileUtil.exist(realFilePath)) {
            originalFileName =  FileUtil.mainName(originalFileName)+"_"+System.currentTimeMillis()
                    +"."+FileUtil.extName(originalFileName);
            realFilePath = typePath + originalFileName;
        }
        File localFile = new File(realFilePath);
        file.transferTo(localFile);
        String url = "http://localhost:9090/files/download/" + type + "/" + originalFileName;
        return Result.success(url);
    }

    //分类文件下载
    @GetMapping("/download/{type}/{fileName}")
    public void download(@PathVariable String type, @PathVariable String fileName, HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        String realFilePath = System.getProperty("user.dir") + "/uploadFiles/" + type + "/" + fileName;
        byte[] bytes = FileUtil.readBytes(realFilePath);
        ServletOutputStream os = response.getOutputStream();
        os.write(bytes);
        os.flush();
        os.close();
    }

}
