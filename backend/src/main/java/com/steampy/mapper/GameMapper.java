package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
/**
 * GameMapper — Game 实体的持久层
 * 继承 BaseMapper<Game>，由 MyBatis-Plus 提供 CRUD
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Game;
import org.apache.ibatis.annotations.Select;
public interface GameMapper extends BaseMapper<Game> {

    /**
     * 查询当前最大的纯数字游戏编号，用于新增游戏时按顺序生成下一个编号。
     *
     * @return 最大数字编号；表内没有纯数字编号时返回 0
     */
    @Select("SELECT COALESCE(MAX(CAST(id AS UNSIGNED)), 0) FROM games WHERE id REGEXP '^[0-9]+$'")
    Long selectMaxNumericId();
}
