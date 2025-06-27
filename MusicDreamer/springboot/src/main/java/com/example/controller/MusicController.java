package com.example.controller;

import com.example.util.Result;
import com.example.entity.Music;
import com.example.service.MusicService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/music")
public class MusicController {
    @Resource
    MusicService musicService;
    @PostMapping("/add")
    public Result add(@RequestBody Music music){
        musicService.add(music);
        return Result.success();
    }
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        musicService.deleteById(id);
        return Result.success();
    }
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids){
        musicService.deleteBatch(ids);
        return Result.success();
    }
    @PutMapping("/update")
    public Result update(@RequestBody Music music){
        musicService.updateById(music);
        return Result.success();
    }
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        Music music = musicService.selectById(id);
        return Result.success(music);
    }
    @GetMapping("/selectAll")
    public Result selectAll(String musicName, Integer fromSinger){
        List<Music> list = musicService.selectAll(musicName, fromSinger);
        return Result.success(list);
    }
    @GetMapping("/selectPage")
    public Result selectPage(
            String musicName,
            Integer fromSinger,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Music> pageInfo = musicService.selectPage(musicName, fromSinger, pageNum, pageSize);
        return Result.success(pageInfo);
    }
} 