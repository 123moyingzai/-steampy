package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Reply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * ReplyMapper — Reply 实体的持久层
 * 继承 BaseMapper<Reply>，由 MyBatis-Plus 提供 CRUD
 */
@Mapper
public interface ReplyMapper extends BaseMapper<Reply> {

    /**
     * 查询指定评论下状态有效的子回复数量
     *
     * @param reviewId 评论 ID
     * @return 子回复数
     */
    @Select("SELECT COUNT(*) FROM review_replies WHERE review_id = #{reviewId} AND status = 1")
    int countRepliesOfReview(@Param("reviewId") String reviewId);

    /**
     * 查询指定用户是否已点赞指定回复（返回 1 表示已点赞）
     *
     * @param replyId 回复 ID
     * @param userId  用户 ID
     * @return 点赞记录数（0 或 1）
     */
    @Select("SELECT COUNT(*) FROM review_reply_likes WHERE reply_id = #{replyId} AND user_id = #{userId}")
    int countLike(@Param("replyId") String replyId, @Param("userId") String userId);

    /**
     * 实时统计某子评论的总点赞数（从 review_reply_likes 表 COUNT）
     *
     * @param replyId 回复 ID
     * @return 总点赞数
     */
    @Select("SELECT COUNT(*) FROM review_reply_likes WHERE reply_id = #{replyId}")
    int countLikes(@Param("replyId") String replyId);
}
