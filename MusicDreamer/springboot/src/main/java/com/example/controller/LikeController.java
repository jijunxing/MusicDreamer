package com.example.controller;

import com.example.entity.Like;
import com.example.service.LikeService;
import com.example.util.Result;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/like")
public class LikeController {
    @Resource
    private LikeService likeService;

    @PostMapping("/add")
    public Result add(@RequestBody Like like) {
        likeService.add(like);
        return Result.success(like.getId());
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        likeService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Like like = likeService.selectById(id);
        return Result.success(like);
    }

    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam(required = false) Integer userId) {
        List<Like> list = likeService.selectAll(userId);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(
            Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Like> pageInfo = likeService.selectPage(userId,pageNum,pageSize);
        return Result.success(pageInfo);
    }

    @DeleteMapping("/deleteBySong")
    public Result deleteBySong(@RequestParam Integer userId, @RequestParam Integer musicId) {
        likeService.deleteByUserAndSong(userId, musicId);
        return Result.success();
    }
} 