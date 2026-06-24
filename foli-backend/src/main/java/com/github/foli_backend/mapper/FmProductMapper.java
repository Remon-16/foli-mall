package com.github.foli_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.foli_backend.entity.FmProduct;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品表 Mapper 接口 Product table Mapper interface
 */
@Mapper
public interface FmProductMapper extends BaseMapper<FmProduct> {
}
