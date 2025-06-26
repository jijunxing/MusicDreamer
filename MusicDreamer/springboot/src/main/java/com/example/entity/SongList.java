package com.example.entity;

import java.util.Date;

public class SongList {
    private Integer id;
    private String name;
    private String image;
    private String message;
    private String tags;
    private Integer user;
    private Date createDate;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Integer getUser() { return user; }
    public void setUser(Integer user) { this.user = user; }

    public Date getCreateDate() { return createDate; }
    public void setCreateDate(Date createDate) { this.createDate = createDate; }
} 