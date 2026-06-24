package com.github.foli_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.foli_backend.entity.FmBalanceLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 余额变动日志表 Mapper 接口 Balance log table Mapper interface
 */
@Mapper
public interface FmBalanceLogMapper extends BaseMapper<FmBalanceLog> {
}
