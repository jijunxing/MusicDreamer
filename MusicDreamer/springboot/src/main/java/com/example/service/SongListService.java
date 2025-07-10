package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Music;
import com.example.entity.SongList;
import com.example.mapper.FavoriteMapper;
import com.example.service.MusicService;
import com.example.mapper.SongListMapper;
import com.example.util.Result;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
public class SongListService {
    @Resource
    private SongListMapper songListMapper;
    @Resource
    private MusicService musicService;
    @Resource
    private FavoriteMapper favoriteMapper;

    private Integer countFavoritesByListId(Integer listId) {
        return favoriteMapper.countBySongListId(listId);
    }
    private void enrichSongListData(SongList songList) {
        // 统计收藏量
        Integer favoriteCount = countFavoritesByListId(songList.getId());
        songList.setFavoriteCount(favoriteCount != null ? favoriteCount : 0);

        // 关联音乐列表
        List<Integer> musicIds = songListMapper.selectMusicIdsByListId(songList.getId());
        List<Music> musics = musicIds.stream()
                .map(musicService::selectById)
                .collect(Collectors.toList());
        songList.setMusics(musics);
    }

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
        if (songList != null) {
            enrichSongListData(songList); // 调用统一增强
        }
        return songList;
    }

    public List<SongList> selectAll(String name, Integer user) {
        List<SongList> lists = songListMapper.selectAll(name, user);
        lists.forEach(this::enrichSongListData); // 遍历增强
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
        return musicService.selectByKeywordExcludingIds(keyword, existingIds);
    }

    public void addMusics(Map<String, Object> body) {
        Integer listId = (Integer) body.get("listId");
        List<Integer> musicIds = (List<Integer>) body.get("musicIds");

        for (Integer musicId : musicIds) {
            songListMapper.addMusicToList(listId, musicId);
        }
    }

}