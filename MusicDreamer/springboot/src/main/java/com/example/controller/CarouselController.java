package com.example.controller;

import com.example.util.Result;
import com.example.entity.Carousel;
import com.example.service.CarouselService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/carousel")
public class CarouselController {
    @Resource
    private CarouselService carouselService;

    @PostMapping("/add")
    public Result add(@RequestBody Carousel carousel) {
        carouselService.add(carousel);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        carouselService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Carousel carousel) {
        carouselService.update(carousel);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Carousel carousel = carouselService.selectById(id);
        return Result.success(carousel);
    }

    @GetMapping("/selectAll")
    public Result selectAll(@RequestParam(required = false) String title,String msg) {
        List<Carousel> list = carouselService.selectAll(title,msg);
        return Result.success(list);
    }

    @GetMapping("/selectPage")
    public Result selectPage(
            String title,
            String msg,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Carousel> pageInfo = carouselService.selectPage(title,msg,pageNum,pageSize);
        return Result.success(pageInfo);
    }
} 