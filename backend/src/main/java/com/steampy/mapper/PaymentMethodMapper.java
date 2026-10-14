package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
/**
 * PaymentMethodMapper — PaymentMethod 实体的持久层
 * 继承 BaseMapper<PaymentMethod>，由 MyBatis-Plus 提供 CRUD
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.PaymentMethod;
public interface PaymentMethodMapper extends BaseMapper<PaymentMethod> {}
