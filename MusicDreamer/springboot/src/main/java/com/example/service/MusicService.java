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
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class MusicService {
    @Resource
    MusicMapper musicMapper;
    @Resource
    TagMapper tagMapper;
    public void add(Music music) {
        music.setActivation(1);
        music.setListenNumb(0);
        music.setCreateTime(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
        music.setTimelength(getMp3Duration(music.getMusicUrl()));
        musicMapper.insert(music);
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
        if (music != null) {
            enrichMusicData(music); // 统一数据增强
        }
        return music;
    }

    public List<Music> selectAll(String musicName, String singerName) {
        List<Music> list = musicMapper.selectAll(musicName, singerName);
        for (Music music : list) {
            enrichMusicData(music);
        }
        return list;
    }

    public PageInfo<Music> selectPage(String musicName, String singerName, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Music> list = this.selectAll(musicName, singerName);
        return PageInfo.of(list);
    }

    public void addWithTags(Music music) {
        musicMapper.insert(music);
        if (music.getTags() != null) {
            List<Integer> tagIds = music.getTags().stream().map(Tag::getId).toList();
            musicMapper.insertMusicTags(music.getMusicId(), tagIds);
        }
        musicMapper.updateById(music);
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

    private void enrichMusicData(Music music) {
        // 添加标签数据
        List<Tag> tags = tagMapper.selectByMusicId(music.getMusicId());
        music.setTags(tags);

        // 添加歌词数据（非数据库字段）
        if (music.getLyricsUrl() != null) {
            System.out.println(downloadAndParseLrc(music.getLyricsUrl()));
            music.setLyrics(downloadAndParseLrc(music.getLyricsUrl()));
        }
    }

    private String downloadAndParseLrc(String lrcUrl) {
        if (lrcUrl == null || lrcUrl.isEmpty()) return null;

        try {
            // 解析并编码URL中的路径部分
            URL originalUrl = new URL(lrcUrl);
            String encodedPath = encodeUrlPath(originalUrl.getPath());

            // 重建包含编码路径的URL
            URL encodedUrl = new URL(
                    originalUrl.getProtocol(),
                    originalUrl.getHost(),
                    originalUrl.getPort(),
                    encodedPath + (originalUrl.getQuery() != null ? "?" + originalUrl.getQuery() : "")
            );
            System.out.println("访问："+encodedUrl);
            HttpURLConnection connection = (HttpURLConnection) encodedUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                return content.toString();
            }
        } catch (Exception e) {
            System.err.println("歌词下载失败: " + lrcUrl + " | 错误: " + e.getMessage());
        }
        return null;
    }

    // 辅助方法：对URL路径部分进行编码
    private String encodeUrlPath(String path) throws UnsupportedEncodingException {
        if (path == null || path.isEmpty()) return "";

        boolean isAbsolutePath = path.startsWith("/");
        String[] segments = path.split("/", -1); // 使用-1参数保留尾部空字符串
        StringBuilder encodedPath = new StringBuilder();

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];

            // 跳过第一个空字符串（如果是绝对路径）
            if (i == 0 && isAbsolutePath && segment.isEmpty()) {
                encodedPath.append("/");
                continue;
            }

            if (!segment.isEmpty()) {
                if (encodedPath.length() > 0 && encodedPath.charAt(encodedPath.length() - 1) != '/') {
                    encodedPath.append("/");
                }
                encodedPath.append(URLEncoder.encode(segment, StandardCharsets.UTF_8)
                        .replace("+", "%20")
                        .replace("%2F", "/"));
            } else if (i > 0) {
                // 处理连续斜杠的情况
                encodedPath.append("/");
            }
        }

        return encodedPath.toString();
    }

    public List<Music> selectByKeywordExcludingIds(String keyword, List<Integer> existingIds) {
        return musicMapper.selectByKeywordExcludingIds(keyword, existingIds);
    }
}