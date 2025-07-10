package com.example.controller;

import com.example.entity.Favorite;
import com.example.service.FavoriteService;
import com.example.util.Result;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/favorite")
public class FavoriteController {
    @Resource
    private FavoriteService favoriteService;

    @PostMapping("/add")
    public Result add(@RequestBody Favorite favorite) {
        favoriteService.add(favorite);
        return Result.success(favorite.getId());
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        favoriteService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Favorite favorite = favoriteService.selectById(id);
        return Result.success(favorite);
    }

    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam(required = false) Integer userId) {
        List<Favorite> list = favoriteService.selectAll(userId);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(
            Integer userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Favorite> pageInfo = favoriteService.selectPage(userId,pageNum,pageSize);
        return Result.success(pageInfo);
    }

    @DeleteMapping("/deleteBySong")
    public Result deleteBySong(@RequestParam Integer userId, @RequestParam Integer songlistId) {
        favoriteService.deleteByUserAndSong(userId, songlistId);
        return Result.success();
    }
} 