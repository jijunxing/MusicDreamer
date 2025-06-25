package com.example.mapper;

import com.example.entity.Music;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface MusicMapper {
    void insert(Music music);
    void deleteById(Integer id);
    void updateById(Music music);
    @Select("select * from music where music_id = #{id}")
    Music selectById(Integer id);
    List<Music> selectAll(String musicName, Integer fromSinger);
} 