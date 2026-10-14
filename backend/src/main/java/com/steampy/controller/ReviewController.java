package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Game;
import com.steampy.entity.Reply;
import com.steampy.entity.Review;
import com.steampy.entity.User;
import com.steampy.mapper.GameMapper;
import com.steampy.mapper.NotificationMapper;
import com.steampy.mapper.ReplyMapper;
import com.steampy.mapper.ReviewMapper;
import com.steampy.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ReviewController
 * 负责游戏评测（评论）及子评论业务的 HTTP 接口层
 * 提供的核心接口：
 *   - GET    /api/reviews/game/{gameId}               — 查询某游戏的评论列表（含实时用户资料、点赞聚合）
 *   - GET    /api/reviews/my?gameId=&userId=          — 查询当前用户对某游戏的评论（用于"编辑我自己的评论"场景）
 *   - POST   /api/reviews                              — 创建或编辑评测（每个用户每个游戏仅可一条；传 id 则为编辑）
 *   - DELETE /api/reviews/{id}                        — 删除自己的评论（同时级联清除点赞记录）
 *   - POST   /api/reviews/{id}/like                   — 对评论点赞（幂等，已点赞则不加数但返回成功）
 *   - DELETE /api/reviews/{id}/like                   — 取消对评论的点赞
 *   - GET    /api/reviews/{reviewId}/replies           — 查询某评论下的所有子评论（扁平列表，含实时点赞聚合）
 *   - POST   /api/reviews/{reviewId}/replies           — 发布子评论（支持回复子评论）
 *   - POST   /api/reviews/replies/{replyId}/like       — 对子评论点赞
 *   - DELETE /api/reviews/replies/{replyId}/like       — 取消对子评论的点赞
 *   - DELETE /api/reviews/replies/{replyId}            — 删除子评论（仅发布人可删）
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired private ReviewMapper reviewMapper;
    @Autowired private ReplyMapper replyMapper;
    @Autowired private NotificationMapper notificationMapper;
    @Autowired private UserMapper userMapper;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询指定游戏的评论列表（按时间倒序）
     * <p>
     * 返回数据会批量回填每个评论发布者的实时昵称、头像（不再存冗余字段），
     * 并实时聚合点赞数（likesCount）。如果传入 userId，还会计算当前用户是否已点赞该评论。
     * </p>
     *
     * @param gameId  游戏 ID
     * @param userId  可选：当前登录用户 ID，用于判断每条评论当前用户是否已点赞；不传则所有 liked 字段为 false
     * @return {@link Result} 包含评论列表
     */
    @GetMapping("/game/{gameId}")
    public Result<List<Review>> listByGame(@PathVariable String gameId,
                                            @RequestParam(required = false) String userId) {
        QueryWrapper<Review> qw = new QueryWrapper<>();
        qw.eq("game_id", gameId).eq("status", 1).orderByDesc("created_at");
        List<Review> list = reviewMapper.selectList(qw);
        Game g = gameMapper.selectById(gameId);
        String name = g != null ? g.getName() : "";
        // 批量回填用户实时资料（昵称 + 头像）
        Map<String, User> userMap = batchGetUsers(list.stream().map(Review::getUserId).collect(Collectors.toList()));
        for (Review r : list) {
            r.setGameName(name);
            fillReviewUserFields(r, userMap);
            // 实时聚合：点赞数 + 当前用户是否已点赞
            r.setLikesCount(reviewMapper.countLikes(r.getId()));
            if (userId != null && !userId.isBlank()) {
                r.setLiked(reviewMapper.countLike(r.getId(), userId) > 0);
            } else {
                r.setLiked(false);
            }
        }
        return Result.success(list);
    }

    /**
     * 查询当前用户对某款游戏的评论（用于"编辑我自己的评论"场景）
     *
     * @param gameId 游戏 ID（Long 类型，因为 games.id 数值较大）
     * @param userId 当前用户 ID
     * @return {@link Result} 包含评论对象（若不存在则 data 为 null）
     */
    @GetMapping("/my")
    public Result<Review> myReview(@RequestParam Long gameId, @RequestParam String userId) {
        QueryWrapper<Review> qw = new QueryWrapper<>();
        qw.eq("game_id", gameId).eq("user_id", userId).last("LIMIT 1");
        Review r = reviewMapper.selectOne(qw);
        if (r != null) r.setLiked(false);
        return Result.success(r);
    }

    /**
     * 创建评测或编辑已有评测
     * <p>
     * 校验逻辑：
     *   1. 内容不少于 5 个字符
     *   2. recommend 必须是 0（不推荐）或 1（推荐）
     * 编辑场景（body 带 id）：只允许评论作者本人编辑；
     * 创建场景（body 不带 id）：每个用户每个游戏只允许一条评论，已有则拒绝并提示"请编辑已有评论"。
     * </p>
     *
     * @param body JSON 请求体，字段包含：
     *             id（可选，存在则为编辑）、gameId、userId、recommend（0/1）、content、images（可选，逗号分隔的图片 URL）
     * @return {@link Result} 成功返回创建/更新后的评论对象
     */
    @PostMapping
    public Result<Review> createOrUpdate(@RequestBody Map<String, Object> body) {
        String id = (String) body.get("id");
        String gameId = String.valueOf(body.get("gameId"));
        String userId = (String) body.get("userId");
        int recommend = Integer.parseInt(String.valueOf(body.get("recommend")));
        String content = (String) body.get("content");
        String images = (String) body.getOrDefault("images", "");

        if (content == null || content.trim().length() < 5) {
            return Result.error("评测内容不少于五个字");
        }
        if (recommend != 0 && recommend != 1) {
            return Result.error("请选择推荐或不推荐");
        }

        // 不再存冗余 userName，查询时从 users 表实时回填

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

    /**
     * 删除自己的评论
     * <p>
     * 仅评论发布人本人可删。删除时级联清理 review_likes 表中该评论的所有点赞记录。
     * </p>
     *
     * @param id     评论 ID
     * @param userId 发起删除请求的用户 ID（需与评论的 userId 一致）
     * @return {@link Result} 成功返回；失败返回错误信息（评论不存在或无权限）
     */
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

    /**
     * 对评论点赞（幂等：已点赞则返回成功但不加数）
     * <p>
     * 禁止给自己点赞。成功点赞时会通过 NotificationController 给评论作者发一条 like_review 类型通知。
     * </p>
     *
     * @param id     评论 ID
     * @param userId 点赞发起者用户 ID
     * @return {@link Result} 包含 liked（布尔，表示当前是否已点赞）和 likesCount（最新点赞总数）
     */
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable String id, @RequestParam String userId) {
        Review r = reviewMapper.selectById(id);
        if (r == null) return Result.error("评论不存在");
        if (r.getUserId().equals(userId)) return Result.error("不能给自己点赞");

        int existing = reviewMapper.countLike(id, userId);
        boolean liked;
        if (existing == 0) {
            jdbcTemplate.update(
                    "INSERT INTO review_likes (review_id, user_id) VALUES (?, ?)", id, userId);
            liked = true;
            // 通知原作者
            if (!r.getUserId().equals(userId)) {
                NotificationController.createNotification(notificationMapper,
                        r.getUserId(), "like_review", userId,
                        "review", id, r.getGameId() == null ? null : String.valueOf(r.getGameId()),
                        null, r.getContent());
            }
        } else {
            liked = true;
        }
        int count = reviewMapper.countLikes(id);
        return Result.success(Map.of("liked", liked, "likesCount", count));
    }

    /**
     * 取消对评论的点赞
     *
     * @param id     评论 ID
     * @param userId 发起取消点赞的用户 ID
     * @return {@link Result} 包含 liked（布尔，表示当前是否已点赞，取消后为 false）和 likesCount（最新点赞总数）
     */
    @DeleteMapping("/{id}/like")
    public Result<Map<String, Object>> unlike(@PathVariable String id, @RequestParam String userId) {
        Review r = reviewMapper.selectById(id);
        if (r == null) return Result.error("评论不存在");

        int existing = reviewMapper.countLike(id, userId);
        boolean liked;
        if (existing > 0) {
            jdbcTemplate.update(
                    "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?", id, userId);
            liked = false;
        } else {
            liked = false;
        }
        int count = reviewMapper.countLikes(id);
        return Result.success(Map.of("liked", liked, "likesCount", count));
    }

    // ========== 子评论（Reply） ==========

    /**
     * 查询某评论下的所有子评论（扁平列表，按创建时间升序）
     * <p>
     * 会批量回填所有回复者和被回复者的实时昵称、头像，
     * 并实时聚合每条子评论的点赞数及当前用户是否已点赞。
     * </p>
     *
     * @param reviewId 父评论 ID
     * @param userId   可选：当前登录用户 ID，用于判断每条子评论当前用户是否已点赞
     * @return {@link Result} 包含子评论列表
     */
    @GetMapping("/{reviewId}/replies")
    public Result<List<Reply>> listReplies(@PathVariable String reviewId,
                                            @RequestParam(required = false) String userId) {
        QueryWrapper<Reply> qw = new QueryWrapper<>();
        qw.eq("review_id", reviewId).eq("status", 1).orderByAsc("created_at");
        List<Reply> list = replyMapper.selectList(qw);
        // 收集所有 userId（回复者 + 被回复者）
        Set<String> allIds = new HashSet<>();
        for (Reply r : list) {
            if (r.getUserId() != null) allIds.add(r.getUserId());
            if (r.getReplyToUserId() != null && !r.getReplyToUserId().isBlank()) allIds.add(r.getReplyToUserId());
        }
        Map<String, User> userMap = batchGetUsers(new ArrayList<>(allIds));
        for (Reply r : list) {
            fillReplyUserFields(r, userMap);
            // 实时聚合点赞数
            r.setLikesCount(replyMapper.countLikes(r.getId()));
            if (userId != null && !userId.isBlank()) {
                r.setLiked(replyMapper.countLike(r.getId(), userId) > 0);
            } else {
                r.setLiked(false);
            }
        }
        return Result.success(list);
    }

    /**
     * 发布子评论（支持回复某条子评论：传 parentReplyId + replyToUserId）
     * <p>
     * 校验逻辑：内容不少于 2 个字符。
     * 成功后会：
     *   1. 父评论的 replies_count +1
     *   2. 通知父评论作者（reply 类型通知）
     *   3. 如果是回复某条子评论，额外通知那条子评论的作者
     * </p>
     *
     * @param reviewId 父评论 ID
     * @param body     JSON 请求体，字段包含：userId（回复者）、content、parentReplyId（可选，被回复的子评论 ID）、replyToUserId（可选，被回复的用户 ID）
     * @return {@link Result} 包含新创建的子评论对象（已回填实时用户资料）
     */
    @PostMapping("/{reviewId}/replies")
    @Transactional
    public Result<Reply> createReply(@PathVariable String reviewId,
                                      @RequestBody Map<String, Object> body) {
        Review parent = reviewMapper.selectById(reviewId);
        if (parent == null) return Result.error("评论不存在");

        String userId = (String) body.get("userId");
        String content = (String) body.get("content");
        if (content == null || content.trim().length() < 2) {
            return Result.error("回复内容不少于两个字");
        }

        Reply r = new Reply();
        r.setId(UUID.randomUUID().toString());
        r.setReviewId(reviewId);
        r.setUserId(userId);
        r.setContent(content.trim());
        r.setLikesCount(0);
        r.setStatus(1);

        // 回复某条子评论（子评论的回复也还是子评论，只是记录了 parentReplyId 便于前端显示 "@xxx"）
        String parentReplyId = (String) body.get("parentReplyId");
        String replyToUserId = (String) body.get("replyToUserId");
        r.setParentReplyId(parentReplyId);
        r.setReplyToUserId(replyToUserId);

        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        replyMapper.insert(r);

        // 父评论 replies_count +1
        jdbcTemplate.update(
                "UPDATE reviews SET replies_count = replies_count + 1 WHERE id = ?", reviewId);

        // 通知父评论作者（不能通知自己）
        if (!parent.getUserId().equals(userId)) {
            NotificationController.createNotification(notificationMapper,
                    parent.getUserId(), "reply", userId,
                    "review", reviewId, parent.getGameId() == null ? null : String.valueOf(parent.getGameId()),
                    content, parent.getContent());
        }
        // 如果回复的是某条子评论，额外通知那条子评论的作者
        if (replyToUserId != null && !replyToUserId.equals(userId)
                && !replyToUserId.equals(parent.getUserId())) {
            String parentReplyContent = null;
            if (parentReplyId != null) {
                Reply parentReply = replyMapper.selectById(parentReplyId);
                if (parentReply != null) parentReplyContent = parentReply.getContent();
            }
            NotificationController.createNotification(notificationMapper,
                    replyToUserId, "reply", userId,
                    "reply", parentReplyId, parent.getGameId() == null ? null : String.valueOf(parent.getGameId()),
                    content, parentReplyContent);
        }

        r.setLiked(false);
        // 回填实时用户资料（前端乐观更新靠这个显示头像/昵称）
        Set<String> ids = new HashSet<>();
        ids.add(userId);
        if (replyToUserId != null && !replyToUserId.isBlank()) ids.add(replyToUserId);
        Map<String, User> userMap = batchGetUsers(new ArrayList<>(ids));
        fillReplyUserFields(r, userMap);
        return Result.success(r);
    }

    /**
     * 对子评论点赞（幂等，重复点赞不加数）
     * <p>
     * 禁止给自己点赞。成功点赞时会给子评论作者发一条 like_reply 类型通知。
     * </p>
     *
     * @param replyId 子评论 ID
     * @param userId  点赞发起者用户 ID
     * @return {@link Result} 包含 liked（布尔）和 likesCount（最新点赞总数）
     */
    @PostMapping("/replies/{replyId}/like")
    public Result<Map<String, Object>> likeReply(@PathVariable String replyId,
                                                  @RequestParam String userId) {
        Reply r = replyMapper.selectById(replyId);
        if (r == null) return Result.error("回复不存在");
        if (r.getUserId().equals(userId)) return Result.error("不能给自己点赞");

        int existing = replyMapper.countLike(replyId, userId);
        if (existing == 0) {
            jdbcTemplate.update(
                    "INSERT INTO review_reply_likes (reply_id, user_id) VALUES (?, ?)", replyId, userId);
            // 通知子评论作者
            if (!r.getUserId().equals(userId)) {
                Review parentReview = reviewMapper.selectById(r.getReviewId());
                String gameId = parentReview != null && parentReview.getGameId() != null
                        ? String.valueOf(parentReview.getGameId()) : null;
                NotificationController.createNotification(notificationMapper,
                        r.getUserId(), "like_reply", userId,
                        "reply", replyId, gameId,
                        null, r.getContent());
            }
        }
        int count = replyMapper.countLikes(replyId);
        return Result.success(Map.of("liked", true, "likesCount", count));
    }

    /**
     * 取消对子评论的点赞
     *
     * @param replyId 子评论 ID
     * @param userId  发起取消点赞的用户 ID
     * @return {@link Result} 包含 liked（布尔，取消后为 false）和 likesCount（最新点赞总数）
     */
    @DeleteMapping("/replies/{replyId}/like")
    public Result<Map<String, Object>> unlikeReply(@PathVariable String replyId,
                                                     @RequestParam String userId) {
        Reply r = replyMapper.selectById(replyId);
        if (r == null) return Result.error("回复不存在");

        int existing = replyMapper.countLike(replyId, userId);
        boolean liked;
        if (existing > 0) {
            jdbcTemplate.update(
                    "DELETE FROM review_reply_likes WHERE reply_id = ? AND user_id = ?", replyId, userId);
            liked = false;
        } else {
            liked = false;
        }
        int count = replyMapper.countLikes(replyId);
        return Result.success(Map.of("liked", liked, "likesCount", count));
    }

    /**
     * 删除子评论（仅发布人本人可删）
     * <p>
     * 删除时：
     *   1. 级联清理 review_reply_likes 表中该子评论的所有点赞记录
     *   2. 父评论的 replies_count -1（使用 GREATEST 避免减到负数）
     * </p>
     *
     * @param replyId 子评论 ID
     * @param userId  发起删除请求的用户 ID（需与子评论的 userId 一致）
     * @return {@link Result} 成功返回；失败返回错误信息（回复不存在或无权限）
     */
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

    // ========== 私有工具：批量回填用户实时资料 ==========

    /**
     * 批量查询 users 表，返回 Map<userId, User>（去重 + 过滤 null）。
     * 用于评论列表场景避免 N+1 查询。
     *
     * @param userIds 用户 ID 列表（可含 null 或重复值，内部会过滤去重）
     * @return Map 结构的用户字典；入参为空或全为 null 时返回空 Map
     */
    private Map<String, User> batchGetUsers(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyMap();
        List<String> unique = userIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (unique.isEmpty()) return Collections.emptyMap();
        List<User> users = userMapper.selectBatchIds(unique);
        Map<String, User> map = new HashMap<>();
        for (User u : users) map.put(u.getId(), u);
        return map;
    }

    /**
     * 给 Review 回填实时 displayName + avatarUrl。
     * <p>
     * 昵称优先取 nickname，取不到再取 username；两者都没有则显示"匿名用户"。
     * 查不到用户（已注销）时显示"已注销用户"并将头像置空。
     * </p>
     *
     * @param r      待回填的评论对象
     * @param userMap 预先批量查询好的用户字典 Map<userId, User>
     */
    private void fillReviewUserFields(Review r, Map<String, User> userMap) {
        User u = userMap.get(r.getUserId());
        if (u != null) {
            String name = (u.getNickname() != null && !u.getNickname().isBlank()) ? u.getNickname() : u.getUsername();
            r.setDisplayName(name != null ? name : "匿名用户");
            r.setAvatarUrl(u.getAvatarUrl());
        } else {
            r.setDisplayName("已注销用户");
            r.setAvatarUrl(null);
        }
    }

    /**
     * 给 Reply 回填双方的实时 displayName + avatarUrl。
     * <p>
     * 同时处理回复者（userId）和被回复者（replyToUserId）两个用户的实时资料回填，
     * 昵称优先 nickname 再 username，注销用户显示"已注销用户"。
     * </p>
     *
     * @param r      待回填的子评论对象
     * @param userMap 预先批量查询好的用户字典 Map<userId, User>
     */
    private void fillReplyUserFields(Reply r, Map<String, User> userMap) {
        // 回复者
        User me = userMap.get(r.getUserId());
        if (me != null) {
            String name = (me.getNickname() != null && !me.getNickname().isBlank()) ? me.getNickname() : me.getUsername();
            r.setDisplayName(name != null ? name : "匿名用户");
            r.setAvatarUrl(me.getAvatarUrl());
        } else {
            r.setDisplayName("已注销用户");
        }
        // 被回复者
        if (r.getReplyToUserId() != null && !r.getReplyToUserId().isBlank()) {
            User target = userMap.get(r.getReplyToUserId());
            if (target != null) {
                String name = (target.getNickname() != null && !target.getNickname().isBlank()) ? target.getNickname() : target.getUsername();
                r.setReplyToDisplayName(name != null ? name : "匿名用户");
                r.setReplyToAvatarUrl(target.getAvatarUrl());
            } else {
                r.setReplyToDisplayName("已注销用户");
            }
        }
    }
}


