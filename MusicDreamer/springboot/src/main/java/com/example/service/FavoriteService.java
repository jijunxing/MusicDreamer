package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Favorite;
import com.example.mapper.FavoriteMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class FavoriteService {
    @Resource
    private FavoriteMapper favoriteMapper;

    public int add(Favorite favorite) {
        favorite.setCreateTime(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
        favoriteMapper.insert(favorite);
        return favorite.getId();
    }

    public void deleteById(Integer id) {
        favoriteMapper.deleteById(id);
    }

    public Favorite selectById(Integer id) {
        return favoriteMapper.selectById(id);
    }

    public List<Favorite> selectAll(Integer userId) {
        return favoriteMapper.selectAll(userId);
    }

    public PageInfo<Favorite> selectPage(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Favorite> list=this.selectAll(userId);
        return PageInfo.of(list);
    }

    public void deleteByUserAndSong(Integer userId, Integer songlistId) {
        favoriteMapper.deleteByUserAndSong(userId,songlistId);
    }
}