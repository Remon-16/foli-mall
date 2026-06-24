package com.github.foli_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.foli_backend.entity.FmStore;
import org.apache.ibatis.annotations.Mapper;

/**
 * 店铺表 Mapper 接口 Store table Mapper interface
 */
@Mapper
public interface FmStoreMapper extends BaseMapper<FmStore> {
}
