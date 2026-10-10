package com.steampy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /** 检查某用户是否点赞了某评论 */
    @Select("SELECT COUNT(*) FROM review_likes WHERE review_id = #{reviewId} AND user_id = #{userId}")
    int countLike(@Param("reviewId") String reviewId, @Param("userId") String userId);

    /** 实时统计某评论的总点赞数（从 review_likes 表 COUNT，不再依赖 likes_count 缓存字段） */
    @Select("SELECT COUNT(*) FROM review_likes WHERE review_id = #{reviewId}")
    int countLikes(@Param("reviewId") String reviewId);
}
