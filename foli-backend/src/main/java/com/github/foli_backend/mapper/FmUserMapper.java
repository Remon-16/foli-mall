package com.github.foli_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.foli_backend.entity.FmUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper 接口 User table Mapper interface
 */
@Mapper
public interface FmUserMapper extends BaseMapper<FmUser> {
}
