package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Notification;
import com.steampy.entity.User;
import com.steampy.mapper.NotificationMapper;
import com.steampy.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * NotificationController
 * 负责站内消息通知的 HTTP 接口层，覆盖消息列表、未读计数、标记已读、以及供业务 Controller 内部调用的通知创建静态工具方法。
 *
 * 实时资料回填：list 接口会批量查出所有 actorId 对应的 User，把 actorName / actorAvatarUrl 实时回填到 Notification，避免 N+1。actorName 优先显示 nickname，fallback username；用户已注销则标注 "已注销用户"。
 * 静态工具方法：{@link #createNotification} 供业务逻辑（订单、评测、提现等）在事务中追加通知使用。
 *
 * 提供的核心接口：
 *   - GET    /api/notifications             — 分页查询通知列表（可按 isRead 过滤）
 *   - GET    /api/notifications/unread-count — 查询未读数
 *   - POST   /api/notifications/{id}/read   — 标记单条已读
 *   - POST   /api/notifications/read-all    — 一键全部标记已读
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationMapper notificationMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    /**
     * 分页查询用户的通知列表。
     *
     * 默认 page=1、size=20；可选 isRead（true/false）过滤。
     * 返回前批量回填 actorName / actorAvatarUrl（按 actorId 查 User 表）。
     *
     * @param userId  用户主键
     * @param page    页码，默认 1
     * @param size    每页条数，默认 20
     * @param isRead  可选，已读/未读过滤
     * @return {@link Result} 含 { list, total, page, size } 的分页 Map
     */
    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam String userId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) Boolean isRead) {
        QueryWrapper<Notification> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        if (isRead != null) {
            qw.eq("is_read", isRead);
        }
        qw.orderByDesc("created_at");
        qw.last("LIMIT " + ((page - 1) * size) + "," + size);

        List<Notification> list = notificationMapper.selectList(qw);
        long total = notificationMapper.selectCount(new QueryWrapper<Notification>().eq("user_id", userId).eq(isRead != null, "is_read", isRead));

        // 批量回填 actorName + actorAvatarUrl
        Set<String> actorIds = new LinkedHashSet<>();
        for (Notification n : list) {
            if (n.getActorId() != null && !n.getActorId().isBlank()) actorIds.add(n.getActorId());
        }
        if (!actorIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(actorIds);
            Map<String, User> userMap = new HashMap<>();
            for (User u : users) userMap.put(u.getId(), u);
            for (Notification n : list) {
                User u = userMap.get(n.getActorId());
                if (u != null) {
                    String name = (u.getNickname() != null && !u.getNickname().isBlank()) ? u.getNickname() : u.getUsername();
                    n.setActorName(name != null ? name : "匿名用户");
                    n.setActorAvatarUrl(u.getAvatarUrl());
                } else {
                    n.setActorName("已注销用户");
                    n.setActorAvatarUrl(null);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success(result);
    }

    /**
     * 查询用户的未读通知数量（is_read=false）。
     *
     * @param userId 用户主键
     * @return {@link Result} 未读条数（Long）
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@RequestParam String userId) {
        QueryWrapper<Notification> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("is_read", false);
        return Result.success(notificationMapper.selectCount(qw));
    }

    /**
     * 标记单条通知为已读（直接用 JdbcTemplate 执行 UPDATE）。
     *
     * @param id 通知主键
     * @return {@link Result} 通用成功响应
     */
    @PostMapping("/{id}/read")
    public Result<?> markRead(@PathVariable String id) {
        jdbcTemplate.update("UPDATE user_notifications SET is_read = 1 WHERE id = ?", id);
        return Result.success();
    }

    /**
     * 一键把某用户所有未读通知全部标记为已读（单条 UPDATE 批量）。
     *
     * @param userId 用户主键
     * @return {@link Result} 通用成功响应
     */
    @PostMapping("/read-all")
    public Result<?> markAllRead(@RequestParam String userId) {
        jdbcTemplate.update("UPDATE user_notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0", userId);
        return Result.success();
    }

    /**
     * 通知创建静态工具方法（供业务 Controller 内部调用）。
     *
     * 会自动做 contentSnippet（限 200 字）和 targetContent（限 500 字）截断，避免长文本入库。
     * actorName / actorAvatarUrl 不存 DB，list 接口实时回填。
     * isRead 初始值固定为 false。
     *
     * @param mapper        NotificationMapper 实例（调用方自行注入）
     * @param userId        通知接收者用户 ID
     * @param type          通知类型（如 "order" / "review" / "system"）
     * @param actorId       触发者用户 ID（可为 null，系统通知时）
     * @param targetType    关联对象类型（如 "order" / "listing" / "review"）
     * @param targetId      关联对象主键
     * @param gameId        关联游戏 ID（可为 null）
     * @param snippet       通知简短内容片段（前端列表展示用）
     * @param targetContent 完整关联内容（可为 null）
     */
    public static void createNotification(NotificationMapper mapper,
                                          String userId, String type, String actorId,
                                          String targetType, String targetId, String gameId,
                                          String snippet, String targetContent) {
        Notification n = new Notification();
        n.setId(UUID.randomUUID().toString());
        n.setUserId(userId);
        n.setType(type);
        n.setActorId(actorId);
        // actorName 不再存 DB，list 接口实时回填
        n.setTargetType(targetType);
        n.setTargetId(targetId);
        n.setGameId(gameId);
        n.setContentSnippet(snippet == null ? "" :
            (snippet.length() > 200 ? snippet.substring(0, 200) : snippet));
        n.setTargetContent(targetContent == null ? null :
            (targetContent.length() > 500 ? targetContent.substring(0, 500) : targetContent));
        n.setIsRead(false);
        mapper.insert(n);
    }

    /**
     * 兼容旧调用的简化版本（targetContent 传 null）。
     *
     * @param mapper     NotificationMapper 实例
     * @param userId     通知接收者用户 ID
     * @param type       通知类型
     * @param actorId    触发者用户 ID
     * @param targetType 关联对象类型
     * @param targetId   关联对象主键
     * @param gameId     关联游戏 ID
     * @param snippet    通知简短内容片段
     */
    public static void createNotification(NotificationMapper mapper,
                                          String userId, String type, String actorId,
                                          String targetType, String targetId, String gameId, String snippet) {
        createNotification(mapper, userId, type, actorId,
                targetType, targetId, gameId, snippet, null);
    }
}
