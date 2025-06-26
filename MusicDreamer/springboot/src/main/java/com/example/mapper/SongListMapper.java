package com.example.mapper;

import com.example.entity.SongList;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SongListMapper {
    int insert(SongList songList);
    int deleteById(Integer id);
    int update(SongList songList);
    SongList selectById(Integer id);
    List<SongList> selectAll(String name, Integer user);
} 