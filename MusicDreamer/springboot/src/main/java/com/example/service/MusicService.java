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
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.springframework.stereotype.Service;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
        music.setTimelength(getMp3Duration(music.getMusicUrl()));
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
        music.setTimelength(getMp3Duration(music.getMusicUrl()));
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

    public List<Music> selectAll(String musicName, String singerName) {
        List<Music> list = musicMapper.selectAll(musicName, singerName);
        for (Music music : list) {
            music.setTags(tagMapper.selectByMusicId(music.getMusicId()));
        }
        return list;
    }

    public PageInfo<Music> selectPage(String musicName, String singerName, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Music> list = this.selectAll(musicName, singerName);
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

    public static Integer getMp3Duration(String url) {
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String path = uri.getPath(); // 获取路径部分：/files/download/music/周杰伦+-+晴天.mp3
        String fileName = path.substring(path.lastIndexOf('/') + 1);
        String truePath = "D:\\IdeaProjects\\MusicDreamer\\uploadFiles\\music\\"+fileName;
        File mp3File = new File(truePath);
        if (!mp3File.exists()) {
            System.err.println("错误: 文件不存在 - " + truePath);
            return 0;
        }

        try {
            MP3File audioFile = (MP3File) AudioFileIO.read(mp3File);
            MP3AudioHeader audioHeader = (MP3AudioHeader) audioFile.getAudioHeader();
            return audioHeader.getTrackLength(); // 秒为单位
        } catch (Exception e) {
            System.err.println("错误: 获取音频时长失败 - " + e.getMessage());
            return 0;
        }
    }
} 