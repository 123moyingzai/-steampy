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
public interface GameMapper extends BaseMapper<Game> {}
