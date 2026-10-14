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

/**
 * AnnouncementController
 * 负责平台公告的 HTTP 接口层，覆盖 C 端公告拉取、已读状态管理、管理员 CRUD。
 *
 * 提供的核心接口：
 *   - GET    /api/announcements          — C 端获取激活的公告（按置顶/发布时间倒序）
 *   - GET    /api/announcements/all      — 管理员获取全部公告
 *   - GET    /api/announcements/unread-count — 查询某用户的公告未读数
 *   - POST   /api/announcements/{id}/read   — 标记单条公告已读
 *   - POST   /api/announcements/read-all    — 一键全部标记已读
 *   - POST   /api/announcements          — 管理员创建公告
 *   - PUT    /api/announcements/{id}     — 管理员更新公告
 *   - DELETE /api/announcements/{id}     — 管理员删除公告
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired private AnnouncementMapper announcementMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    /**
     * C 端获取激活公告（只返回 is_active=true 的）。
     *
     * 排序规则：置顶优先（is_top 降序），其次按发布时间 publish_date 降序。
     *
     * @return {@link Result} 激活公告列表
     */
    @GetMapping
    public Result<List<Announcement>> getAnnouncements() {
        QueryWrapper<Announcement> qw = new QueryWrapper<>();
        qw.eq("is_active", true).orderByDesc("is_top").orderByDesc("publish_date");
        return Result.success(announcementMapper.selectList(qw));
    }

    /**
     * 管理员获取全部公告（含未激活的）。
     *
     * @return {@link Result} 按置顶 + 发布时间倒序的公告列表
     */
    @GetMapping("/all")
    public Result<List<Announcement>> getAll() {
        QueryWrapper<Announcement> qw = new QueryWrapper<>();
        qw.orderByDesc("is_top").orderByDesc("publish_date");
        return Result.success(announcementMapper.selectList(qw));
    }

    /**
     * 查询某用户的公告未读数（激活公告总数 - 已读数，负数时归 0）。
     *
     * 已读数基于 {@code announcement_reads} 表的 JOIN 统计。
     *
     * @param userId 用户主键
     * @return {@link Result} 未读数量（Long，>= 0）
     */
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

    /**
     * 标记单条公告已读（幂等，INSERT IGNORE 防重复）。
     *
     * @param id     公告主键
     * @param userId 用户主键
     * @return {@link Result} 通用成功响应
     */
    @PostMapping("/{id}/read")
    public Result<?> markRead(@PathVariable String id, @RequestParam String userId) {
        jdbcTemplate.update(
                "INSERT IGNORE INTO announcement_reads (announcement_id, user_id, read_at) VALUES (?, ?, ?)",
                id, userId, LocalDateTime.now());
        return Result.success();
    }

    /**
     * 一键全部标记已读（把所有激活公告中用户还没读过的都补插入到 reads 表）。
     *
     * 使用 INSERT IGNORE + SELECT 子查询一次性完成，避免 N 次单条 INSERT。
     *
     * @param userId 用户主键
     * @return {@link Result} 通用成功响应
     */
    @PostMapping("/read-all")
    public Result<?> markAllRead(@RequestParam String userId) {
        // 把所有活跃公告中用户还没读过的都插入 reads 表
        String sql = "INSERT IGNORE INTO announcement_reads (announcement_id, user_id, read_at) " +
                "SELECT id, ?, ? FROM announcements WHERE is_active = 1";
        jdbcTemplate.update(sql, userId, LocalDateTime.now());
        return Result.success();
    }

    /**
     * 管理员创建公告。
     *
     * 默认值补全：publishDate 空 → 当前时间；isTop 空 → false；isActive 空 → true。
     *
     * @param a Announcement 请求体
     * @return {@link Result} 插入后的 Announcement
     */
    @PostMapping
    public Result<Announcement> create(@RequestBody Announcement a) {
        if (a.getPublishDate() == null) a.setPublishDate(LocalDateTime.now().toString());
        if (a.getIsTop() == null) a.setIsTop(false);
        if (a.getIsActive() == null) a.setIsActive(true);
        announcementMapper.insert(a);
        return Result.success(a);
    }

    /**
     * 管理员更新公告（按路径 id 覆盖 body.id）。
     *
     * @param id 公告主键
     * @param a  更新内容
     * @return {@link Result} 更新后的 Announcement
     */
    @PutMapping("/{id}")
    public Result<Announcement> update(@PathVariable String id, @RequestBody Announcement a) {
        a.setId(id);
        announcementMapper.updateById(a);
        return Result.success(a);
    }

    /**
     * 管理员删除公告（物理删除）。
     *
     * @param id 公告主键
     * @return {@link Result} 通用成功响应
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable String id) {
        announcementMapper.deleteById(id);
        return Result.success();
    }
}

