package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
/**
 * WalletMapper — Wallet 实体的持久层
 * 继承 BaseMapper<Wallet>，由 MyBatis-Plus 提供 CRUD
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Wallet;
public interface WalletMapper extends BaseMapper<Wallet> {}
