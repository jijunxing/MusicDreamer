package com.example.controller;

import com.example.common.Result;
import com.example.entity.Log;
import com.example.service.LogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/log")
public class LogController {
    @Resource
    private LogService logService;

    @PostMapping("/add")
    public Result add(@RequestBody Log log) {
        logService.add(log);
        return Result.success();
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam("createDate") String createDate,
                        @RequestParam("userName") String userName,
                        @RequestParam("musicName") String musicName) {
        // 这里建议用主键，但log表无主键，只能用复合条件
        logService.deleteById(java.sql.Date.valueOf(createDate), userName, musicName);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Log log) {
        logService.update(log);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam(required = false) String userName,
                            @RequestParam(required = false) String musicName) {
        List<Log> list = logService.selectAll(userName, musicName);
        return Result.success(list);
    }
} 