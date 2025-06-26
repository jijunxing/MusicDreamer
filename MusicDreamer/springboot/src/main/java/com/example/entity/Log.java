package com.example.entity;

import java.util.Date;

public class Log {
    private Date createDate;
    private String doSome;
    private String musicName;
    private String userName;

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getDoSome() {
        return doSome;
    }

    public void setDoSome(String doSome) {
        this.doSome = doSome;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
} 