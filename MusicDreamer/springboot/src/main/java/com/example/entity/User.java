package com.example.entity;

import lombok.Data;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private String avatar;
    private String role;
    private String phone;
    private String email;
    private String about;
    private String createTime;
    private Integer activation;
    private String sex;
}
