package com.example.entity;

import lombok.Data;

import java.util.List;

@Data
public class Music {
    private Integer musicId;
    private String musicName;
    private String musicUrl;
    private String imageUrl;
    private Integer fromSinger;
    private String singerName;
    private String lyric;
    private Integer timelength;
    private Integer activation;
    private String creatTime;
    private Integer listenNumb;
    private List<Tag> tags;
} 