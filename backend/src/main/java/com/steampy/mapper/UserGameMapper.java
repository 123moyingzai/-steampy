package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
/**
 * UserGameMapper — UserGame 实体的持久层
 * 继承 BaseMapper<UserGame>，由 MyBatis-Plus 提供 CRUD
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.UserGame;
public interface UserGameMapper extends BaseMapper<UserGame> {}
