package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
/**
 * AnnouncementMapper — Announcement 实体的持久层
 * 继承 BaseMapper<Announcement>，由 MyBatis-Plus 提供 CRUD
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Announcement;
public interface AnnouncementMapper extends BaseMapper<Announcement> {}
