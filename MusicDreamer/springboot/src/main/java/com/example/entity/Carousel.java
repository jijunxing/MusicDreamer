package com.example.entity;

import lombok.Data;

@Data
public class Carousel {
    private Integer id;
    private String title;
    private String imageUrl;
    private Integer userId;
    private String userName;
    private String createTime;
    private String relatedType;
    private Integer relatedId;
}
