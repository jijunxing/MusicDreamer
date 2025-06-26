package com.example.entity;

import java.util.Date;
import lombok.Data;

@Data
public class SongList {
    private Integer id;
    private String name;
    private String image;
    private String message;
    private String tags;
    private Integer user;
    private Date createDate;
} 