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
 * ReportController
 * 负责内容举报业务的 HTTP 接口层
 * 提供的核心接口：
 *   - POST /api/content/report  — 举报评论（review）或子评论（reply）
 *
 * 独立路径 /api/content 避免与 ReviewController 的 /api/reviews/{id} 路径变量冲突。
 *
 * @author steampy
 * @date 2026-10-14
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

    /**
     * 举报评论或子评论
     * <p>
     * 校验逻辑：
     *   1. targetType 必须是 review 或 reply
     *   2. 被举报内容必须存在，且举报人不能举报自己的内容
     *   3. 同一举报人对同一目标只能举报一次（防重复）
     * 成功后会同步对目标内容的 report_count +1，便于后台筛选。
     * </p>
     *
     * @param targetType 举报目标类型：review（评论） 或 reply（子评论）
     * @param targetId   被举报目标的 ID
     * @param reporterId 举报人用户 ID
     * @param reason     可选，举报原因描述
     * @return {@link Result} 成功返回；失败返回错误信息（参数非法 / 内容不存在 / 举报自己 / 重复举报）
     */
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
