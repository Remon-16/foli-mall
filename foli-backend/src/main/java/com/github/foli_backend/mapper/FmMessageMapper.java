package com.github.foli_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.foli_backend.entity.FmMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息表 Mapper 接口 Message table Mapper interface
 */
@Mapper
public interface FmMessageMapper extends BaseMapper<FmMessage> {
}
