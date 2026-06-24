package com.github.foli_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.foli_backend.entity.FmOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单表 Mapper 接口 Order table Mapper interface
 */
@Mapper
public interface FmOrderMapper extends BaseMapper<FmOrder> {
}
