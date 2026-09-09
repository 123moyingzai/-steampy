package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Game;
import com.steampy.entity.Reply;
import com.steampy.entity.Review;
import com.steampy.mapper.GameMapper;
import com.steampy.mapper.NotificationMapper;
import com.steampy.mapper.ReplyMapper;
import com.steampy.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired private ReviewMapper reviewMapper;
    @Autowired private ReplyMapper replyMapper;
    @Autowired private NotificationMapper notificationMapper;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询游戏评论列表
     * @param userId 可选，传了就返回每条评论当前用户是否已点赞
     */
    @GetMapping("/game/{gameId}")
    public Result<List<Review>> listByGame(@PathVariable Long gameId,
                                            @RequestParam(required = false) String userId) {
        QueryWrapper<Review> qw = new QueryWrapper<>();
        qw.eq("game_id", gameId).eq("status", 1).orderByDesc("created_at");
        List<Review> list = reviewMapper.selectList(qw);
        Game g = gameMapper.selectById(gameId);
        String name = g != null ? g.getName() : "";
        for (Review r : list) {
            r.setGameName(name);
            if (userId != null && !userId.isBlank()) {
                r.setLiked(reviewMapper.countLike(r.getId(), userId) > 0);
            } else {
                r.setLiked(false);
            }
        }
        return Result.success(list);
    }

    @GetMapping("/my")
    public Result<Review> myReview(@RequestParam Long gameId, @RequestParam String userId) {
        QueryWrapper<Review> qw = new QueryWrapper<>();
        qw.eq("game_id", gameId).eq("user_id", userId).last("LIMIT 1");
        Review r = reviewMapper.selectOne(qw);
        if (r != null) r.setLiked(false);
        return Result.success(r);
    }

    @PostMapping
    public Result<Review> createOrUpdate(@RequestBody Map<String, Object> body) {
        String id = (String) body.get("id");
        Long gameId = Long.valueOf(body.get("gameId").toString());
        String userId = (String) body.get("userId");
        String userName = (String) body.getOrDefault("userName", "");
        Integer recommend = Integer.valueOf(body.get("recommend").toString());
        String content = (String) body.get("content");
        String images = (String) body.getOrDefault("images", "");

        if (content == null || content.trim().length() < 5) {
            return Result.error("评测内容不少于五个字");
        }
        if (recommend != 0 && recommend != 1) {
            return Result.error("请选择推荐或不推荐");
        }

        Review review;
        if (id != null && !id.isBlank()) {
            review = reviewMapper.selectById(id);
            if (review == null || !review.getUserId().equals(userId)) {
                return Result.error("评论不存在或无权限编辑");
            }
            review.setRecommend(recommend);
            review.setContent(content.trim());
            review.setImages(images);
            review.setStatus(1);
            review.setUpdatedAt(LocalDateTime.now());
            reviewMapper.updateById(review);
        } else {
            QueryWrapper<Review> qw = new QueryWrapper<>();
            qw.eq("game_id", gameId).eq("user_id", userId);
            Review existed = reviewMapper.selectOne(qw);
            if (existed != null) {
                return Result.error("每个用户每个游戏仅可评论一次，请编辑已有评论");
            }
            review = new Review();
            review.setId(UUID.randomUUID().toString());
            review.setGameId(gameId);
            review.setUserId(userId);
            review.setUserName(userName);
            review.setRecommend(recommend);
            review.setContent(content.trim());
            review.setImages(images);
            review.setStatus(1);
            review.setLikesCount(0);
            review.setCreatedAt(LocalDateTime.now());
            review.setUpdatedAt(LocalDateTime.now());
            reviewMapper.insert(review);
        }
        review.setLiked(false);
        return Result.success(review);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable String id, @RequestParam String userId) {
        Review r = reviewMapper.selectById(id);
        if (r == null || !r.getUserId().equals(userId)) {
            return Result.error("评论不存在或无权限删除");
        }
        // 连点赞记录一起清
        jdbcTemplate.update("DELETE FROM review_likes WHERE review_id = ?", id);
        reviewMapper.deleteById(id);
        return Result.success();
    }

    /** 点赞（幂等：已点赞则返回成功但不加数） */
    @PostMapping("/{id}/like")
    @Transactional
    public Result<Map<String, Object>> like(@PathVariable String id, @RequestParam String userId) {
        Review r = reviewMapper.selectById(id);
        if (r == null) return Result.error("评论不存在");
        if (r.getUserId().equals(userId)) return Result.error("不能给自己点赞");

        int existing = reviewMapper.countLike(id, userId);
        boolean liked;
        if (existing == 0) {
            jdbcTemplate.update(
                    "INSERT INTO review_likes (review_id, user_id) VALUES (?, ?)", id, userId);
            reviewMapper.incrementLikesCount(id, 1);
            liked = true;
            // 通知原作者
            if (!r.getUserId().equals(userId)) {
                NotificationController.createNotification(notificationMapper,
                        r.getUserId(), "like_review", userId, null,
                        "review", id, r.getGameId() == null ? null : String.valueOf(r.getGameId()), r.getContent());
            }
        } else {
            liked = true; // 已经点过了，不算错误
        }
        int count = r.getLikesCount() == null ? 0 : r.getLikesCount();
        if (liked && existing == 0) count++;
        return Result.success(Map.of("liked", liked, "likesCount", count));
    }

    /** 取消点赞 */
    @DeleteMapping("/{id}/like")
    @Transactional
    public Result<Map<String, Object>> unlike(@PathVariable String id, @RequestParam String userId) {
        Review r = reviewMapper.selectById(id);
        if (r == null) return Result.error("评论不存在");

        int existing = reviewMapper.countLike(id, userId);
        boolean liked;
        int count = r.getLikesCount() == null ? 0 : r.getLikesCount();
        if (existing > 0) {
            jdbcTemplate.update(
                    "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?", id, userId);
            reviewMapper.incrementLikesCount(id, -1);
            count = Math.max(0, count - 1);
            liked = false;
        } else {
            liked = false;
        }
        return Result.success(Map.of("liked", liked, "likesCount", count));
    }

    // ========== 子评论（Reply） ==========

    /** 查某评论下的所有子评论（扁平列表，按时间排） */
    @GetMapping("/{reviewId}/replies")
    public Result<List<Reply>> listReplies(@PathVariable String reviewId,
                                            @RequestParam(required = false) String userId) {
        QueryWrapper<Reply> qw = new QueryWrapper<>();
        qw.eq("review_id", reviewId).eq("status", 1).orderByAsc("created_at");
        List<Reply> list = replyMapper.selectList(qw);
        for (Reply r : list) {
            if (userId != null && !userId.isBlank()) {
                r.setLiked(replyMapper.countLike(r.getId(), userId) > 0);
            } else {
                r.setLiked(false);
            }
        }
        return Result.success(list);
    }

    /** 发布子评论（支持回复某条子评论：传 parentReplyId + replyToUserId/Name） */
    @PostMapping("/{reviewId}/replies")
    @Transactional
    public Result<Reply> createReply(@PathVariable String reviewId,
                                      @RequestBody Map<String, Object> body) {
        Review parent = reviewMapper.selectById(reviewId);
        if (parent == null) return Result.error("评论不存在");

        String userId = (String) body.get("userId");
        String userName = (String) body.getOrDefault("userName", "");
        String content = (String) body.get("content");
        if (content == null || content.trim().length() < 2) {
            return Result.error("回复内容不少于两个字");
        }

        Reply r = new Reply();
        r.setId(UUID.randomUUID().toString());
        r.setReviewId(reviewId);
        r.setUserId(userId);
        r.setUserName(userName);
        r.setContent(content.trim());
        r.setLikesCount(0);
        r.setStatus(1);

        // 回复某条子评论（子评论的回复也还是子评论，只是记录了 parentReplyId 便于前端显示 "@xxx"）
        String parentReplyId = (String) body.get("parentReplyId");
        String replyToUserId = (String) body.get("replyToUserId");
        String replyToUserName = (String) body.get("replyToUserName");
        r.setParentReplyId(parentReplyId);
        r.setReplyToUserId(replyToUserId);
        r.setReplyToUserName(replyToUserName);

        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        replyMapper.insert(r);

        // 父评论 replies_count +1
        jdbcTemplate.update(
                "UPDATE reviews SET replies_count = replies_count + 1 WHERE id = ?", reviewId);

        // 通知父评论作者（不能通知自己）
        if (!parent.getUserId().equals(userId)) {
            NotificationController.createNotification(notificationMapper,
                    parent.getUserId(), "reply", userId, userName,
                    "review", reviewId, parent.getGameId() == null ? null : String.valueOf(parent.getGameId()), content);
        }
        // 如果回复的是某条子评论，额外通知那条子评论的作者
        if (replyToUserId != null && !replyToUserId.equals(userId)
                && !replyToUserId.equals(parent.getUserId())) {
            NotificationController.createNotification(notificationMapper,
                    replyToUserId, "reply", userId, userName,
                    "reply", parentReplyId, parent.getGameId() == null ? null : String.valueOf(parent.getGameId()), content);
        }

        r.setLiked(false);
        return Result.success(r);
    }

    /** 子评论点赞 */
    @PostMapping("/replies/{replyId}/like")
    @Transactional
    public Result<Map<String, Object>> likeReply(@PathVariable String replyId,
                                                  @RequestParam String userId) {
        Reply r = replyMapper.selectById(replyId);
        if (r == null) return Result.error("回复不存在");
        if (r.getUserId().equals(userId)) return Result.error("不能给自己点赞");

        int existing = replyMapper.countLike(replyId, userId);
        if (existing == 0) {
            jdbcTemplate.update(
                    "INSERT INTO review_reply_likes (reply_id, user_id) VALUES (?, ?)", replyId, userId);
            jdbcTemplate.update(
                    "UPDATE review_replies SET likes_count = likes_count + 1 WHERE id = ?", replyId);
            r.setLikesCount((r.getLikesCount() == null ? 0 : r.getLikesCount()) + 1);
            // 通知子评论作者
            if (!r.getUserId().equals(userId)) {
                Review parentReview = reviewMapper.selectById(r.getReviewId());
                String gameId = parentReview != null && parentReview.getGameId() != null
                        ? String.valueOf(parentReview.getGameId()) : null;
                NotificationController.createNotification(notificationMapper,
                        r.getUserId(), "like_reply", userId, null,
                        "reply", replyId, gameId, r.getContent());
            }
        }
        return Result.success(Map.of("liked", true, "likesCount", r.getLikesCount()));
    }

    /** 子评论取消点赞 */
    @DeleteMapping("/replies/{replyId}/like")
    @Transactional
    public Result<Map<String, Object>> unlikeReply(@PathVariable String replyId,
                                                     @RequestParam String userId) {
        Reply r = replyMapper.selectById(replyId);
        if (r == null) return Result.error("回复不存在");

        int existing = replyMapper.countLike(replyId, userId);
        boolean liked;
        int count = r.getLikesCount() == null ? 0 : r.getLikesCount();
        if (existing > 0) {
            jdbcTemplate.update(
                    "DELETE FROM review_reply_likes WHERE reply_id = ? AND user_id = ?", replyId, userId);
            jdbcTemplate.update(
                    "UPDATE review_replies SET likes_count = likes_count - 1 WHERE id = ?", replyId);
            count = Math.max(0, count - 1);
            liked = false;
        } else {
            liked = false;
        }
        return Result.success(Map.of("liked", liked, "likesCount", count));
    }

    /** 删除子评论（发布人或父评论作者可删） */
    @DeleteMapping("/replies/{replyId}")
    @Transactional
    public Result<?> deleteReply(@PathVariable String replyId, @RequestParam String userId) {
        Reply r = replyMapper.selectById(replyId);
        if (r == null) return Result.error("回复不存在");
        // 只有自己能删
        if (!r.getUserId().equals(userId)) return Result.error("无权限删除");

        jdbcTemplate.update("DELETE FROM review_reply_likes WHERE reply_id = ?", replyId);
        replyMapper.deleteById(replyId);
        jdbcTemplate.update(
                "UPDATE reviews SET replies_count = GREATEST(replies_count - 1, 0) WHERE id = ?",
                r.getReviewId());
        return Result.success();
    }
}
