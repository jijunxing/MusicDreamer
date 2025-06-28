package com.example.mapper;

import com.example.entity.Tag;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TagMapper {
    @Select("SELECT * FROM tag")
    List<Tag> selectAll();

    @Select("SELECT t.* FROM tag t JOIN music_tag mt ON t.id = mt.tag_id WHERE mt.music_id = #{musicId}")
    List<Tag> selectByMusicId(Integer musicId);
}
