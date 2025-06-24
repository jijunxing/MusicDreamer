package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService {

    @Resource
    UserMapper userMapper;

    public User login(User user) {
        String username = user.getUsername();
        //根据账号查询数据
        User dbUser = userMapper.selectByUsername(username);
        if(dbUser == null) {
            throw new CustomException("账号不存在");
        }
        else if(dbUser.getActivation()==0) {
            throw new CustomException("账号被冻结");
        }
        else if(!dbUser.getRole().equals(user.getRole())) {
            throw new CustomException("请检查用户类型是否选择错误");
        }
        //校验密码
        else if(!dbUser.getPassword().equals(user.getPassword())){
            throw new CustomException("账号或密码错误");
        }
        return dbUser;
    }

    //往数据库添加数据
    public void register(User user) {
        //校验用户账号是否存在
        User dbUser = userMapper.selectByUsername(user.getUsername());
        if(dbUser != null) {
            throw new CustomException("用户名已存在");
        }
        //校验密码是否为空
        if(ObjectUtil.isEmpty(user.getPassword())) {
            throw new CustomException("密码不能为空");
        }
        user.setCreateTime(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
        userMapper.insert(user);
    }

    public void add(User user) {
        User dbUser = userMapper.selectByUsername(user.getUsername());
        if(dbUser != null) {
            throw new CustomException("账号已存在");
        }
        if(ObjectUtil.isEmpty(user.getPassword())) {
            user.setPassword(user.getUsername()); //默认密码
        }
        user.setRole("USER");
        userMapper.insert(user);
    }

    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }

    public void updateById(User user) {
        userMapper.updateById(user);
    }

    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    public List<User> selectAll(String username, String role) {
        return userMapper.selectAll(username, role);
    }

    public PageInfo<User> selectPage(String username, String role, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list=this.selectAll(username, role);
        return PageInfo.of(list);
    }
}
