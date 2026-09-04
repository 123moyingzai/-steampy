package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.Game;
import com.steampy.entity.Review;
import com.steampy.mapper.GameMapper;
import com.steampy.mapper.ReviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private GameMapper gameMapper;

    @GetMapping("/game/{gameId}")
    public Result<List<Review>> listByGame(@PathVariable Long gameId) {
        QueryWrapper<Review> qw = new QueryWrapper<>();
        qw.eq("game_id", gameId).eq("status", 1).orderByDesc("created_at");
        List<Review> list = reviewMapper.selectList(qw);
        Game g = gameMapper.selectById(gameId);
        String name = g != null ? g.getName() : "";
        list.forEach(r -> r.setGameName(name));
        return Result.success(list);
    }

    @GetMapping("/my")
    public Result<Review> myReview(@RequestParam Long gameId, @RequestParam String userId) {
        QueryWrapper<Review> qw = new QueryWrapper<>();
        qw.eq("game_id", gameId).eq("user_id", userId).last("LIMIT 1");
        Review r = reviewMapper.selectOne(qw);
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
            review.setCreatedAt(LocalDateTime.now());
            review.setUpdatedAt(LocalDateTime.now());
            reviewMapper.insert(review);
        }
        return Result.success(review);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable String id, @RequestParam String userId) {
        Review r = reviewMapper.selectById(id);
        if (r == null || !r.getUserId().equals(userId)) {
            return Result.error("评论不存在或无权限删除");
        }
        reviewMapper.deleteById(id);
        return Result.success();
    }
}
