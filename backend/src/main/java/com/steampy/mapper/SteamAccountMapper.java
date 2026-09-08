package com.steampy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.SteamAccount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SteamAccountMapper extends BaseMapper<SteamAccount> {
}
