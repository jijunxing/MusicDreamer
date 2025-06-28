package com.example.controller;

import com.example.util.Result;
import com.example.entity.SongList;
import com.example.service.SongListService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/songlist")
public class SongListController {
    @Resource
    private SongListService songListService;

    @PostMapping("/add")
    public Result add(@RequestBody SongList songList) {
        songListService.add(songList);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        songListService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody SongList songList) {
        songListService.update(songList);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        SongList songList = songListService.selectById(id);
        return Result.success(songList);
    }

    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam(required = false) String name,
                            @RequestParam(required = false) Integer user) {
        List<SongList> list = songListService.selectAll(name, user);
        return Result.success(list);
    }
} 