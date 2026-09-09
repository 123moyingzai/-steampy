package com.steampy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Reply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReplyMapper extends BaseMapper<Reply> {

    @Select("SELECT COUNT(*) FROM review_replies WHERE review_id = #{reviewId} AND status = 1")
    int countRepliesOfReview(@Param("reviewId") String reviewId);

    @Select("SELECT COUNT(*) FROM review_reply_likes WHERE reply_id = #{replyId} AND user_id = #{userId}")
    int countLike(@Param("replyId") String replyId, @Param("userId") String userId);
}
