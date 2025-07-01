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
    private String lyricUrl;
    private Integer timelength;
    private Integer activation;
    private String createTime;
    private Integer listenNumb;
    private List<Tag> tags;
    private List<Integer> tagIds; // 非数据库字段，用于接收前端传来的标签

} 