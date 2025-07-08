package com.example.entity;

import lombok.Data;

@Data
public class Favorite {
    private Integer id;
    private Integer userId;
    private Integer songListId;
    private String createTime;
}
