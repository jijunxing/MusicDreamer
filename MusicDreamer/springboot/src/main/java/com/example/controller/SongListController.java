package com.example.controller;

import com.example.entity.Music;
import com.example.mapper.SongListMapper;
import com.example.service.MusicService;
import com.example.util.Result;
import com.example.entity.SongList;
import com.example.service.SongListService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@CrossOrigin
@RestController
@RequestMapping("/songlist")
public class SongListController {
    @Resource
    private SongListService songListService;
    @Resource
    private SongListMapper songListMapper;
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

    @PostMapping("/addMusic")
    public Result addMusicToList(@RequestParam Integer listId, @RequestParam Integer musicId) {
        songListService.addMusicToList(listId, musicId);
        return Result.success();
    }

    @DeleteMapping("/removeMusic")
    public Result removeMusicFromList(@RequestParam Integer listId, @RequestParam Integer musicId) {
        songListService.removeMusicFromList(listId, musicId);
        return Result.success();
    }

    @GetMapping("/selectPage")
    public Result selectPage(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer user,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(songListService.selectPage(name, user, pageNum, pageSize));
    }

    @GetMapping("/selectForSonglist")
    public Result selectForSonglist(
            @RequestParam(required = false) String keyword,
            @RequestParam Integer excludeSonglistId) {
        List<Music> musics = songListService.selectForSonglist(keyword,excludeSonglistId);
        return Result.success(musics);
    }

    @PostMapping("/addMusics")
    public Result addMusics(@RequestBody Map<String, Object> body) {
        songListService.addMusics(body);
        return Result.success();
    }

} 