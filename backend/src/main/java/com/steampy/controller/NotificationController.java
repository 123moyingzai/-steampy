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

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired private NotificationMapper notificationMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @GetMapping
    public Result<List<Notification>> list(@RequestParam String userId) {
        QueryWrapper<Notification> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("created_at").last("LIMIT 50");
        List<Notification> list = notificationMapper.selectList(qw);
        // 批量回填最新的 actorName + actorAvatarUrl（不再存快照，实时查 users 表）
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
        return Result.success(list);
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

    /** 兼容旧调用（targetContent 传 null） */
    public static void createNotification(NotificationMapper mapper,
                                          String userId, String type, String actorId,
                                          String targetType, String targetId, String gameId, String snippet) {
        createNotification(mapper, userId, type, actorId,
                targetType, targetId, gameId, snippet, null);
    }
}
