package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.jwt.JwtUtil;
import com.example.mapper.UserMapper;
import com.example.security.UserDetailsImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Resource
    UserMapper userMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    public Map login(User user) {
        String username = user.getUsername();
        String password = user.getPassword();
//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
//        //从数据库中查询对应的信息
//        User dbUser = userMapper.selectByUsername(username);
//        if(dbUser == null) {
//            throw new CustomException("账号不存在");
//        }
//        else if(dbUser.getActivation()==0) {
//            throw new CustomException("账号被冻结");
//        }
//        else if(!dbUser.getRole().equals(user.getRole())) {
//            throw new CustomException("请检查用户类型是否选择错误");
//        }
//        //校验密码
//        else if(!passwordEncoder.matches(password, dbUser.getPassword()))
//            throw new CustomException("用户名或密码错误");
//        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
//        UserDetailsImpl authUser = (UserDetailsImpl) authenticate.getPrincipal();
//        dbUser = authUser.getUser();// 强制类型转换报错，用我们实现的 `UserDetailsImpl` 类中的 getUser 方法
////        dbUser=((UserDetailsImpl)authenticate.getPrincipal()).getUser();
//        String jwt = JwtUtil.createJWT(dbUser.getId().toString());
//        dbUser.setJwt(jwt);
//        return dbUser;

        // 1. 构建认证令牌
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        // 2. 调用Spring Security认证管理器（内部会调用UserDetailsService）
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 获取认证后的用户信息
        UserDetailsImpl authUser = (UserDetailsImpl) authentication.getPrincipal();
        User dbUser = authUser.getUser();

        // 4. 生成JWT并返回
        String jwt_token = JwtUtil.createJWT(dbUser.getId().toString(),dbUser.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("user", dbUser);
        result.put("jwt_token", jwt_token);
        return result;
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
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);//加密密码
        user.setCreateTime(DateUtil.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
        userMapper.insert(user);
    }

    public void add(User user) {
        User dbUser = userMapper.selectByUsername(user.getUsername());
        if(dbUser != null) {
            throw new CustomException("账号已存在");
        }
        if(ObjectUtil.isEmpty(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getUsername())); //默认密码为用户名
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
        if(user.getPassword()!=null)
            user.setPassword(passwordEncoder.encode(user.getPassword()));
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
