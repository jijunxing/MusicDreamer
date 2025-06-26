package com.example.mapper;

import com.example.entity.Log;
import org.apache.ibatis.annotations.Mapper;
import java.util.Date;
import java.util.List;

@Mapper
public interface LogMapper {
    int insert(Log log);
    int deleteById(Date createDate, String userName, String musicName);
    int update(Log log);
    List<Log> selectAll(String userName, String musicName);
} 