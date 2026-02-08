package com.ygoj.record.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ygoj.record.pojo.Record;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecordMapper extends BaseMapper<Record> {
}
