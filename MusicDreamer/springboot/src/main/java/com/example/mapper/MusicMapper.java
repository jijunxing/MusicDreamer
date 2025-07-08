package com.example.mapper;

import com.example.entity.Music;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface MusicMapper {

    @Options(useGeneratedKeys = true, keyColumn = "music_id", keyProperty = "musicId")
    void insert(Music music);
    void deleteById(Integer id);
    void updateById(Music music);
    @Select("select music.*, user.username as singerName from music left join user on music.from_singer = user.id where music_id = #{id}")
    Music selectById(Integer id);
    List<Music> selectAll(String musicName, String singerName);
    void insertMusicTags(Integer musicId, List<Integer> tagIds);
    void deleteMusicTags(Integer musicId);

    void deleteTagsByMusicId(Integer musicId);

    void insertMusicTag(Integer musicId, Integer tagId);

    void freezeById(Integer id);
    void unfreezeById(Integer id);

    List<Music> selectByKeywordExcludingIds(String keyword, List<Integer> existingIds);
}