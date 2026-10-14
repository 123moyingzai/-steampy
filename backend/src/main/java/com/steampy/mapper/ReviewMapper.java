package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * ReviewMapper — Review 实体的持久层
 * 继承 BaseMapper<Review>，由 MyBatis-Plus 提供 CRUD
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /**
     * 检查某用户是否点赞了某评论（返回 1 表示已点赞）
     *
     * @param reviewId 评论 ID
     * @param userId   用户 ID
     * @return 点赞记录数（0 或 1）
     */
    @Select("SELECT COUNT(*) FROM review_likes WHERE review_id = #{reviewId} AND user_id = #{userId}")
    int countLike(@Param("reviewId") String reviewId, @Param("userId") String userId);

    /**
     * 实时统计某评论的总点赞数（从 review_likes 表 COUNT，不再依赖 likes_count 缓存字段）
     *
     * @param reviewId 评论 ID
     * @return 总点赞数
     */
    @Select("SELECT COUNT(*) FROM review_likes WHERE review_id = #{reviewId}")
    int countLikes(@Param("reviewId") String reviewId);
}
