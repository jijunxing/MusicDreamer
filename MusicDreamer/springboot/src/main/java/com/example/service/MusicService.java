package com.example.service;

import com.example.entity.Music;
import com.example.exception.CustomException;
import com.example.mapper.MusicMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MusicService {
    @Resource
    MusicMapper musicMapper;

    public void add(Music music) {
        musicMapper.insert(music);
    }

    public void deleteById(Integer id) {
        musicMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }

    public void updateById(Music music) {
        musicMapper.updateById(music);
    }

    public Music selectById(Integer id) {
        return musicMapper.selectById(id);
    }

    public List<Music> selectAll(String musicName, Integer fromSinger) {
        return musicMapper.selectAll(musicName, fromSinger);
    }

    public PageInfo<Music> selectPage(String musicName, Integer fromSinger, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Music> list = this.selectAll(musicName, fromSinger);
        return PageInfo.of(list);
    }
} 