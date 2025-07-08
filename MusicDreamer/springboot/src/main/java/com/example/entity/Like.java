package com.example.entity;

import lombok.Data;

@Data
public class Like {
    private Integer id;
    private Integer userId;
    private Integer musicId;
    private String createTime;
}
