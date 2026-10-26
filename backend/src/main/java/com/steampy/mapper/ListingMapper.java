package com.steampy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.Listing;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ListingMapper extends BaseMapper<Listing> {
}
