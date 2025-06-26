package com.example.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Log {
    private Date createDate;
    private String doSome;
    private String musicName;
    private String userName;
} 