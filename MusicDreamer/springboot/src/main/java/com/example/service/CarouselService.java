package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Carousel;
import com.example.mapper.CarouselMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CarouselService {
    @Resource
    private CarouselMapper carouselMapper;

    public int add(Carousel carousel) {
        carousel.setCreateTime(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
        return carouselMapper.insert(carousel);
    }

    public int deleteById(Integer id) {
        return carouselMapper.deleteById(id);
    }

    public int update(Carousel carousel) {
        return carouselMapper.update(carousel);
    }

    public Carousel selectById(Integer id) {
        return carouselMapper.selectById(id);
    }

    public List<Carousel> selectAll(String title,String msg) {
        return carouselMapper.selectAll(title,msg);
    }

    public PageInfo<Carousel> selectPage(String title, String msg, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Carousel> list=this.selectAll(title, msg);
        return PageInfo.of(list);
    }
} 