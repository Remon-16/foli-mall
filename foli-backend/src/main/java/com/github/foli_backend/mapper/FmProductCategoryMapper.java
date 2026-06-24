package com.github.foli_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.foli_backend.entity.FmProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类表 Mapper 接口 Product category table Mapper interface
 */
@Mapper
public interface FmProductCategoryMapper extends BaseMapper<FmProductCategory> {
}
