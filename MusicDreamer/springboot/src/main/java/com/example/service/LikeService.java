package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Like;
import com.example.mapper.LikeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class LikeService {
    @Resource
    private LikeMapper likeMapper;

    public int add(Like like) {
        like.setCreateTime(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
        likeMapper.insert(like);
        return like.getId();
    }

    public void deleteById(Integer id) {
        likeMapper.deleteById(id);
    }

    public Like selectById(Integer id) {
        return likeMapper.selectById(id);
    }

    public List<Like> selectAll(Integer userId) {
        return likeMapper.selectAll(userId);
    }

    public PageInfo<Like> selectPage(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Like> list=this.selectAll(userId);
        return PageInfo.of(list);
    }

    public void deleteByUserAndSong(Integer userId, Integer musicId) {
        likeMapper.deleteByUserAndSong(userId,musicId);
    }
}