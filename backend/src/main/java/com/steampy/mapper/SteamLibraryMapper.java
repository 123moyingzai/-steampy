package com.steampy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.SteamLibrary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SteamLibraryMapper extends BaseMapper<SteamLibrary> {
}
