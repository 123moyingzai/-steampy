package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
/**
 * OrderMapper — Order 实体的持久层
 * 继承 BaseMapper<Order>，由 MyBatis-Plus 提供 CRUD
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Order;
public interface OrderMapper extends BaseMapper<Order> {}
