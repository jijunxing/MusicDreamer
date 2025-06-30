package com.example.mapper;

import com.example.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface NoticeMapper {
    int insert(Notice notice);
    int deleteById(Integer id);
    int update(Notice notice);
    Notice selectById(Integer id);
    List<Notice> selectAll(String title,String msg);
} 