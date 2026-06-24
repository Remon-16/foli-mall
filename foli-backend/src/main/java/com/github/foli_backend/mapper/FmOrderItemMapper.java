package com.github.foli_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.foli_backend.entity.FmOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项表 Mapper 接口 Order item table Mapper interface
 */
@Mapper
public interface FmOrderItemMapper extends BaseMapper<FmOrderItem> {
}
