package com.example.mapper;

import com.example.entity.Like;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.List;

@Mapper
public interface LikeMapper {

    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void insert(Like like);

    void deleteById(Integer id);
    Like selectById(Integer id);
    List<Like> selectAll(Integer userId);
    @Delete("delete from like_music where user_id = #{userId} and music_id = #{musicId}")
    void deleteByUserAndSong(Integer userId, Integer musicId);
}