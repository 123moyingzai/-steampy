package com.steampy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Game;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface GameMapper extends BaseMapper<Game> {

    /**
     * 取当前最大的纯数字 id，用于新增游戏时生成下一个 id（games.id 为 varchar）。
     *
     * @return 最大数字 id，表为空时返回 0
     */
    @Select("SELECT COALESCE(MAX(CAST(id AS UNSIGNED)), 0) FROM games WHERE id REGEXP '^[0-9]+$'")
    Long selectMaxNumericId();

    /**
     * 查询所有已被逻辑删除的游戏（绕过 @TableLogic 过滤）。
     * 管理员重新添加同名游戏时用它恢复原记录，保证 id 不变、原有 CDK 可继续上架。
     *
     * @return 已删除的游戏列表
     */
    @Select("SELECT * FROM games WHERE is_deleted = 1")
    List<Game> selectDeletedGames();

    /**
     * 恢复逻辑删除的游戏（is_deleted 置 0）。
     *
     * @param id 游戏主键
     * @return 受影响行数
     */
    @Update("UPDATE games SET is_deleted = 0 WHERE id = #{id}")
    int restoreById(@Param("id") String id);
}
