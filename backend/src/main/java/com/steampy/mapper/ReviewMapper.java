package com.steampy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /** 检查某用户是否点赞了某评论 */
    @Select("SELECT COUNT(*) FROM review_likes WHERE review_id = #{reviewId} AND user_id = #{userId}")
    int countLike(@Param("reviewId") String reviewId, @Param("userId") String userId);

    /** 给某评论 likes_count + delta（可为 -1） */
    @Update("UPDATE reviews SET likes_count = likes_count + #{delta} WHERE id = #{reviewId}")
    void incrementLikesCount(@Param("reviewId") String reviewId, @Param("delta") int delta);
}
