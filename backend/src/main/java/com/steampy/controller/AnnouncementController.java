package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Announcement;
import com.steampy.mapper.AnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @GetMapping
    public Result<List<Announcement>> getAnnouncements() {
        QueryWrapper<Announcement> qw = new QueryWrapper<>();
        qw.eq("is_active", true).orderByDesc("is_top").orderByDesc("publish_date");
        return Result.success(announcementMapper.selectList(qw));
    }

    @GetMapping("/all")
    public Result<List<Announcement>> getAll() {
        QueryWrapper<Announcement> qw = new QueryWrapper<>();
        qw.orderByDesc("is_top").orderByDesc("publish_date");
        return Result.success(announcementMapper.selectList(qw));
    }

    @PostMapping
    public Result<Announcement> create(@RequestBody Announcement a) {
        if (a.getPublishDate() == null) a.setPublishDate(LocalDateTime.now().toString());
        if (a.getIsTop() == null) a.setIsTop(false);
        if (a.getIsActive() == null) a.setIsActive(true);
        announcementMapper.insert(a);
        return Result.success(a);
    }

    @PutMapping("/{id}")
    public Result<Announcement> update(@PathVariable Long id, @RequestBody Announcement a) {
        a.setId(id);
        announcementMapper.updateById(a);
        return Result.success(a);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        return Result.success();
    }
}
