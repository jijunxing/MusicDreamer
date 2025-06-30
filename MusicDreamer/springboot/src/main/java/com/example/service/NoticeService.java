package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Notice;
import com.example.entity.User;
import com.example.mapper.NoticeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NoticeService {
    @Resource
    private NoticeMapper noticeMapper;

    public int add(Notice notice) {
        notice.setCreateTime(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
        return noticeMapper.insert(notice);
    }

    public int deleteById(Integer id) {
        return noticeMapper.deleteById(id);
    }

    public int update(Notice notice) {
        return noticeMapper.update(notice);
    }

    public Notice selectById(Integer id) {
        return noticeMapper.selectById(id);
    }

    public List<Notice> selectAll(String title,String msg) {
        return noticeMapper.selectAll(title,msg);
    }

    public PageInfo<Notice> selectPage(String title, String msg, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Notice> list=this.selectAll(title, msg);
        return PageInfo.of(list);
    }
} 