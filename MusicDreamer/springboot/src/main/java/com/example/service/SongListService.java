package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Music;
import com.example.entity.SongList;
import com.example.mapper.MusicMapper;
import com.example.mapper.SongListMapper;
import com.example.util.Result;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
public class SongListService {
    @Resource
    private SongListMapper songListMapper;
    @Resource
    private MusicMapper musicMapper;

    public int add(SongList songList) {
        songList.setCreateTime(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
        return songListMapper.insert(songList);
    }

    public int deleteById(Integer id) {
        return songListMapper.deleteById(id);
    }

    public int update(SongList songList) {
        return songListMapper.update(songList);
    }

    public SongList selectById(Integer id) {
        SongList songList = songListMapper.selectById(id);
        List<Integer> musicIds = songListMapper.selectMusicIdsByListId(id);
        List<Music> musics = new ArrayList<>();
        for (Integer musicId : musicIds) {
            musics.add(musicMapper.selectById(musicId));
        }
        songList.setMusics(musics);
        return songList;
    }

    public List<SongList> selectAll(String name, Integer user) {
        List<SongList> lists = songListMapper.selectAll(name, user);
        for (SongList songList : lists) {
            List<Integer> musicIds = songListMapper.selectMusicIdsByListId(songList.getId());
            List<Music> musics = new ArrayList<>();
            for (Integer musicId : musicIds) {
                musics.add(musicMapper.selectById(musicId));
            }
            songList.setMusics(musics);
        }
        return lists;
    }

    public void addMusicToList(Integer listId, Integer musicId) {
        songListMapper.addMusicToList(listId, musicId);
    }

    public void removeMusicFromList(Integer listId, Integer musicId) {
        songListMapper.removeMusicFromList(listId, musicId);
    }

    public PageInfo<SongList> selectPage(String name, Integer user, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<SongList> list = this.selectAll(name, user);
        return PageInfo.of(list);
    }

    public List<Music> selectForSonglist(String keyword, Integer excludeSonglistId) {
        List<Integer> existingIds = songListMapper.selectMusicIdsByListId(excludeSonglistId);
        return musicMapper.selectByKeywordExcludingIds(keyword, existingIds);
    }

    public void addMusics(Map<String, Object> body) {
        Integer listId = (Integer) body.get("listId");
        List<Integer> musicIds = (List<Integer>) body.get("musicIds");

        for (Integer musicId : musicIds) {
            songListMapper.addMusicToList(listId, musicId);
        }
    }

}