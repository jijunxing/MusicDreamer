package com.example.entity;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class SongList {
    private Integer id;
    private String name;
    private String imageUrl;
    private String about;
    private Integer user;
    private String userName;
    private String createTime;
    private List<Music> musics;
    private Integer musicCount;
    private Integer favoriteCount;
} 