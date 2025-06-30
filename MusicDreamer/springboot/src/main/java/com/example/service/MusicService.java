package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Music;
import com.example.entity.Tag;
import com.example.exception.CustomException;
import com.example.mapper.MusicMapper;
import com.example.mapper.TagMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MusicService {
    @Resource
    MusicMapper musicMapper;
    @Resource
    TagMapper tagMapper;
    public Integer add(Music music) {
        music.setActivation(1);
        music.setListenNumb(0);
        music.setCreateTime(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
        musicMapper.insert(music);
        return music.getMusicId();
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
        musicMapper.deleteMusicTags(music.getMusicId()); // 清空旧标签

        if (music.getTags() != null) {
            List<Integer> tagIds = music.getTags().stream().map(Tag::getId).toList();
            musicMapper.insertMusicTags(music.getMusicId(), tagIds);
        }
        musicMapper.updateById(music);
    }

    public Music selectById(Integer id) {
        Music music = musicMapper.selectById(id);
        List<Tag> tags = tagMapper.selectByMusicId(id);
        music.setTags(tags);
        return music;
    }

    public List<Music> selectAll(String musicName, Integer fromSinger) {
        List<Music> list = musicMapper.selectAll(musicName, fromSinger);
        for (Music music : list) {
            music.setTags(tagMapper.selectByMusicId(music.getMusicId()));
        }
        return list;
    }

    public PageInfo<Music> selectPage(String musicName, Integer fromSinger, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Music> list = this.selectAll(musicName, fromSinger);
        return PageInfo.of(list);
    }

    public void bindTags(Integer musicId, List<Integer> tagIds) {
        // 删除旧绑定
        musicMapper.deleteTagsByMusicId(musicId);
        // 添加新绑定
        for (Integer tagId : tagIds) {
            musicMapper.insertMusicTag(musicId, tagId);
        }
    }

    public void freezeById(Integer id) {
        musicMapper.freezeById(id);
    }

    public void unfreezeById(Integer id) {
        musicMapper.unfreezeById(id);
    }

} 