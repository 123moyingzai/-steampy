package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
/**
 * BankCardMapper — BankCard 实体的持久层
 * 继承 BaseMapper<BankCard>，由 MyBatis-Plus 提供 CRUD
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.BankCard;
public interface BankCardMapper extends BaseMapper<BankCard> {}
