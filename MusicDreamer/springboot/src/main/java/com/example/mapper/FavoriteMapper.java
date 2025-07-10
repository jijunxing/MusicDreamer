package com.example.mapper;

import com.example.entity.Favorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void insert(Favorite favorite);

    void deleteById(Integer id);
    Favorite selectById(Integer id);
    List<Favorite> selectAll(Integer userId);
    @Delete("delete from favorite_songlist where user_id = #{userId} and songlist_id = #{songlistId}")
    void deleteByUserAndSong(Integer userId, Integer listId);
}