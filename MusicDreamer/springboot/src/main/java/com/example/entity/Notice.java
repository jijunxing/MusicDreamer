package com.example.entity;

import lombok.Data;

@Data
public class Notice {
    private Integer id;
    private String title;
    private Integer userId ;
    private String msg;
    private String createTime;
    private String userName;
}
