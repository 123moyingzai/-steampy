package com.steampy.controller;

import com.steampy.dto.Result;
import com.steampy.entity.Reply;
import com.steampy.entity.Review;
import com.steampy.mapper.ReplyMapper;
import com.steampy.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 举报接口（独立路径避免 /api/reviews/{id} 路径变量冲突）
 */
@RestController
@RequestMapping("/api/content")
public class ReportController {

    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private ReplyMapper replyMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/report")
    @Transactional
    public Result<?> report(@RequestParam String targetType,
                            @RequestParam String targetId,
                            @RequestParam String reporterId,
                            @RequestParam(required = false) String reason) {
        if (!"review".equals(targetType) && !"reply".equals(targetType)) {
            return Result.error("targetType 必须是 review 或 reply");
        }
        if ("review".equals(targetType)) {
            Review r = reviewMapper.selectById(targetId);
            if (r == null) return Result.error("评论不存在");
            if (r.getUserId().equals(reporterId)) return Result.error("不能举报自己的评论");
        } else {
            Reply r = replyMapper.selectById(targetId);
            if (r == null) return Result.error("回复不存在");
            if (r.getUserId().equals(reporterId)) return Result.error("不能举报自己的回复");
        }
        Integer dup = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reports WHERE target_type = ? AND target_id = ? AND reporter_id = ?",
                Integer.class, targetType, targetId, reporterId);
        if (dup != null && dup > 0) return Result.error("你已举报过该内容");

        jdbcTemplate.update(
                "INSERT INTO reports (id, target_type, target_id, reporter_id, reason, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                UUID.randomUUID().toString(), targetType, targetId, reporterId,
                reason == null ? "" : reason, LocalDateTime.now());
        if ("review".equals(targetType)) {
            jdbcTemplate.update("UPDATE reviews SET report_count = report_count + 1 WHERE id = ?", targetId);
        } else {
            jdbcTemplate.update("UPDATE review_replies SET report_count = report_count + 1 WHERE id = ?", targetId);
        }
        return Result.success();
    }
}
