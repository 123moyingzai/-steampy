package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Announcement;
import com.steampy.mapper.AnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired private AnnouncementMapper announcementMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

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

    /** 官方公告未读数（用户还没读过的已激活公告数） */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@RequestParam String userId) {
        // 激活公告总数 - 用户已读数
        Long total = announcementMapper.selectCount(
                new QueryWrapper<Announcement>().eq("is_active", true));
        Long read = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM announcement_reads ar JOIN announcements a " +
                "ON ar.announcement_id = a.id WHERE ar.user_id = ? AND a.is_active = 1",
                Long.class, userId);
        long unread = (total == null ? 0 : total) - (read == null ? 0 : read);
        return Result.success(Math.max(0, unread));
    }

    /** 标记一条公告已读 */
    @PostMapping("/{id}/read")
    public Result<?> markRead(@PathVariable Long id, @RequestParam String userId) {
        jdbcTemplate.update(
                "INSERT IGNORE INTO announcement_reads (announcement_id, user_id, read_at) VALUES (?, ?, ?)",
                id, userId, LocalDateTime.now());
        return Result.success();
    }

    /** 全部标记已读 */
    @PostMapping("/read-all")
    public Result<?> markAllRead(@RequestParam String userId) {
        // 把所有活跃公告中用户还没读过的都插入 reads 表
        String sql = "INSERT IGNORE INTO announcement_reads (announcement_id, user_id, read_at) " +
                "SELECT id, ?, ? FROM announcements WHERE is_active = 1";
        jdbcTemplate.update(sql, userId, LocalDateTime.now());
        return Result.success();
    }

    /** 管理员：创建 */
    @PostMapping
    public Result<Announcement> create(@RequestBody Announcement a) {
        if (a.getPublishDate() == null) a.setPublishDate(LocalDateTime.now().toString());
        if (a.getIsTop() == null) a.setIsTop(false);
        if (a.getIsActive() == null) a.setIsActive(true);
        announcementMapper.insert(a);
        return Result.success(a);
    }

    /** 管理员：更新 */
    @PutMapping("/{id}")
    public Result<Announcement> update(@PathVariable Long id, @RequestBody Announcement a) {
        a.setId(id);
        announcementMapper.updateById(a);
        return Result.success(a);
    }

    /** 管理员：删除 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        return Result.success();
    }
}
