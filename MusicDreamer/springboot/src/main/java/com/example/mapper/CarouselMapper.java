package com.example.mapper;

import com.example.entity.Carousel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CarouselMapper {
    int insert(Carousel carousel);
    int deleteById(Integer id);
    int update(Carousel carousel);
    Carousel selectById(Integer id);
    List<Carousel> selectAll(String title,String msg);
} 