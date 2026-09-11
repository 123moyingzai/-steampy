package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Notification;
import com.steampy.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationMapper notificationMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @GetMapping
    public Result<List<Notification>> list(@RequestParam String userId) {
        QueryWrapper<Notification> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("created_at").last("LIMIT 50");
        return Result.success(notificationMapper.selectList(qw));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@RequestParam String userId) {
        QueryWrapper<Notification> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("is_read", false);
        return Result.success(notificationMapper.selectCount(qw));
    }

    @PostMapping("/{id}/read")
    public Result<?> markRead(@PathVariable String id) {
        jdbcTemplate.update("UPDATE user_notifications SET is_read = 1 WHERE id = ?", id);
        return Result.success();
    }

    @PostMapping("/read-all")
    public Result<?> markAllRead(@RequestParam String userId) {
        jdbcTemplate.update("UPDATE user_notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0", userId);
        return Result.success();
    }

    /** 供业务 Controller 内部调用：创建一条通知 */
    public static void createNotification(NotificationMapper mapper,
                                          String userId, String type, String actorId, String actorName,
                                          String targetType, String targetId, String gameId,
                                          String snippet, String targetContent) {
        Notification n = new Notification();
        n.setId(UUID.randomUUID().toString());
        n.setUserId(userId);
        n.setType(type);
        n.setActorId(actorId);
        n.setActorName(actorName);
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

    /** 兼容旧调用（targetContent 传 null） */
    public static void createNotification(NotificationMapper mapper,
                                          String userId, String type, String actorId, String actorName,
                                          String targetType, String targetId, String gameId, String snippet) {
        createNotification(mapper, userId, type, actorId, actorName,
                targetType, targetId, gameId, snippet, null);
    }
}
