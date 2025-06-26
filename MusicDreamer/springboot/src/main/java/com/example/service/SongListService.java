package com.example.service;

import com.example.entity.SongList;
import com.example.mapper.SongListMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SongListService {
    @Resource
    private SongListMapper songListMapper;

    public int add(SongList songList) {
        return songListMapper.insert(songList);
    }

    public int deleteById(Integer id) {
        return songListMapper.deleteById(id);
    }

    public int update(SongList songList) {
        return songListMapper.update(songList);
    }

    public SongList selectById(Integer id) {
        return songListMapper.selectById(id);
    }

    public List<SongList> selectAll(String name, Integer user) {
        return songListMapper.selectAll(name, user);
    }
} 