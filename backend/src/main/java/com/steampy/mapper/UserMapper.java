package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
/**
 * UserMapper — User 实体的持久层
 * 继承 BaseMapper<User>，由 MyBatis-Plus 提供 CRUD
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.User;
public interface UserMapper extends BaseMapper<User> {}
