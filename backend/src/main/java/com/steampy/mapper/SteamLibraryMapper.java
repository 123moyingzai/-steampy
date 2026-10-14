package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.SteamLibrary;
import org.apache.ibatis.annotations.Mapper;

/**
 * SteamLibraryMapper — SteamLibrary 实体的持久层
 * 继承 BaseMapper<SteamLibrary>，由 MyBatis-Plus 提供 CRUD
 */
@Mapper
public interface SteamLibraryMapper extends BaseMapper<SteamLibrary> {
}
