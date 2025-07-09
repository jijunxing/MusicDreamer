package com.example.controller;

import com.example.exception.CustomException;
import com.example.util.Result;
import com.example.entity.User;
import com.example.service.UserService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;
    /*新增接口*/
    @PostMapping("/add")
    public Result add(@RequestBody User user){
        userService.add(user);
        return Result.success();
    }

    /*删除*/
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        userService.deleteById(id);
        return Result.success();
    }

    /*批量删除*/
    @DeleteMapping("/delete/batch")
    public Result delete(@RequestBody List<Integer> ids){
        userService.deleteBatch(ids);
        return Result.success();
    }

    /*修改*/
    @PutMapping("/update")
    public Result update(@RequestBody User user){
        userService.updateById(user);
        return Result.success();
    }

    /*单个查询*/
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        User user =userService.selectById(id);
        return Result.success(user);
    }

    /*全部查询*/
    @GetMapping("/selectAll")
    public Result selectAll(String username, String role){
        List<User> list =userService.selectAll(username, role);
        return Result.success(list);
    }

    /*分页查询*/
    @GetMapping("/selectPage")
    public Result selectPage(
            String username,
            String role,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<User> pageInfo=userService.selectPage(username,role,pageNum,pageSize);
        return Result.success(pageInfo);
    }

    @PostMapping("/changePwd")
    public Result changePwd(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.error("参数不能为空");
        }

        try {
            userService.changePwd(oldPassword, newPassword);
            return Result.success("密码修改成功");
        } catch (CustomException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("修改密码失败，请重试");
        }
    }
}
