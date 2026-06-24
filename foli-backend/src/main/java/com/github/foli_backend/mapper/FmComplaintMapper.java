package com.github.foli_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.foli_backend.entity.FmComplaint;
import org.apache.ibatis.annotations.Mapper;

/**
 * 投诉表 Mapper 接口 Complaint table Mapper interface
 */
@Mapper
public interface FmComplaintMapper extends BaseMapper<FmComplaint> {
}
