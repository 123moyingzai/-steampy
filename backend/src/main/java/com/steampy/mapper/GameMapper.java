package com.steampy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Game;
import org.apache.ibatis.annotations.Select;

public interface GameMapper extends BaseMapper<Game> {

    /**
     * 取当前最大的纯数字 id，用于新增游戏时生成下一个 id（games.id 为 varchar）。
     *
     * @return 最大数字 id，表为空时返回 0
     */
    @Select("SELECT COALESCE(MAX(CAST(id AS UNSIGNED)), 0) FROM games WHERE id REGEXP '^[0-9]+$'")
    Long selectMaxNumericId();
}
