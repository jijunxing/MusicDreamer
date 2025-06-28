package com.example.controller;

import com.example.entity.Tag;
import com.example.mapper.TagMapper;
import com.example.util.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    TagMapper tagMapper;

    @GetMapping("/list")
    public Result list() {
        List<Tag> list = tagMapper.selectAll();
        return Result.success(list);
    }
}
