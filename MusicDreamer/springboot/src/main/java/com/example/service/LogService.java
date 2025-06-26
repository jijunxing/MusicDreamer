package com.example.service;

import com.example.entity.Log;
import com.example.mapper.LogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LogService {
    @Resource
    private LogMapper logMapper;

    public int add(Log log) {
        return logMapper.insert(log);
    }

    public int deleteById(java.util.Date createDate, String userName, String musicName) {
        return logMapper.deleteById(createDate, userName, musicName);
    }

    public int update(Log log) {
        return logMapper.update(log);
    }

    public List<Log> selectAll(String userName, String musicName) {
        return logMapper.selectAll(userName, musicName);
    }
} 