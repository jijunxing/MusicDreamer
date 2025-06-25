package com.example.entity;

public class Music {
    private Integer musicId;
    private String musicName;
    private String musicUrl;
    private String imageUrl;
    private Integer fromSinger;
    private String lyric;
    private Integer timelength;
    private Integer activation;
    private String creatTime;
    private Integer listenNumb;

    public Integer getMusicId() {
        return musicId;
    }

    public void setMusicId(Integer musicId) {
        this.musicId = musicId;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getFromSinger() {
        return fromSinger;
    }

    public void setFromSinger(Integer fromSinger) {
        this.fromSinger = fromSinger;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public Integer getTimelength() {
        return timelength;
    }

    public void setTimelength(Integer timelength) {
        this.timelength = timelength;
    }

    public Integer getActivation() {
        return activation;
    }

    public void setActivation(Integer activation) {
        this.activation = activation;
    }

    public String getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(String creatTime) {
        this.creatTime = creatTime;
    }

    public Integer getListenNumb() {
        return listenNumb;
    }

    public void setListenNumb(Integer listenNumb) {
        this.listenNumb = listenNumb;
    }
} 