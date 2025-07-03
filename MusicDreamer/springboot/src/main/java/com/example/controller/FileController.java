package com.example.controller;

import cn.hutool.core.io.FileUtil;
import com.example.util.Result;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final String filePath = System.getProperty("user.dir") + "/uploadFiles/";

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "type", defaultValue = "other") String type) throws IOException {
        String basePath = System.getProperty("user.dir") + "/uploadFiles/";
        String typePath = basePath + type + "/";
        if(!FileUtil.exist(typePath)) {
            FileUtil.mkdir(typePath);
        }

        // 仅去除空格（不移除其他字符）
        String sanitized = sanitizeFileName(file.getOriginalFilename());
        String baseName = FileUtil.mainName(sanitized);
        String ext = FileUtil.extName(sanitized);

        // 生成最终存储名
        String storageName = baseName + "." + ext;
        String realPath = typePath + storageName;

        // 重名处理
        if (FileUtil.exist(realPath)) {
            storageName = baseName + "_" + UUID.randomUUID() + "." + ext;
            realPath = typePath + storageName;
        }

        file.transferTo(new File(realPath));

        // 取消URL编码
        String url = "http://localhost:9090/files/download/" + type + "/" + storageName;

        return Result.success(url);
    }

    @GetMapping("/download/{type}/{fileName}")
    public void download(@PathVariable String type,
                         @PathVariable String fileName,
                         HttpServletResponse response) throws IOException {
        // 取消URL解码
        String realName = fileName; // 直接使用原始文件名
        String filePath = System.getProperty("user.dir") + "/uploadFiles/" + type + "/" + realName;

        // 设置响应头（空格已去除，无需还原）
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + realName + "\"");

        byte[] bytes = FileUtil.readBytes(filePath);
        ServletOutputStream os = response.getOutputStream();
        os.write(bytes);
        os.flush();
        os.close();
    }

    // 简化清洗方法（仅去除空格）
    private String sanitizeFileName(String name) {
        // 仅替换空格为空字符串（不移除其他字符）
        return name.replaceAll(" ", "");
    }
}