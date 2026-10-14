package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
/**
 * TransactionMapper — Transaction 实体的持久层
 * 继承 BaseMapper<Transaction>，由 MyBatis-Plus 提供 CRUD
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Transaction;
public interface TransactionMapper extends BaseMapper<Transaction> {}
