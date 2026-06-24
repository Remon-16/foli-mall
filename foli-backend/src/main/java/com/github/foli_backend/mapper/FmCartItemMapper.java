package com.github.foli_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.foli_backend.entity.FmCartItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车表 Mapper 接口 Cart item table Mapper interface
 */
@Mapper
public interface FmCartItemMapper extends BaseMapper<FmCartItem> {
}
