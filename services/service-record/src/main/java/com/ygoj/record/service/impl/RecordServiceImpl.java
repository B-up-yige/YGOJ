package com.ygoj.record.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ygoj.record.mapper.RecordMapper;
import com.ygoj.record.Record;
import com.ygoj.record.service.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RecordServiceImpl implements RecordService {
    @Autowired
    private RecordMapper recordMapper;

    /**
     * 通过提交记录id获取提交记录信息
     *
     * @param id 提交记录id
     * @return {@link Record}
     */
    @Override
    public Record getRecordinfoById(Long id) {
        Record record = recordMapper.selectById(id);
        return record;
    }

    /**
     * 添加提交记录
     *
     * @param record 提交记录
     */
    @Override
    @GlobalTransactional
    public void addRecord(Record record) {
        //TODO:数据检验
        //检验userId
        //检验problemId
        recordMapper.insert(record);
    }

    /**
     * 编辑提交记录状态
     *
     * @param id     提交记录id
     * @param status 状态
     */
    @Override
    @GlobalTransactional
    public void editRecordStatus(Long id, String status) {
        Record record = recordMapper.selectById(id);
        record.setStatus(status);
        recordMapper.updateById(record);
    }

    /**
     * 分页获取提交列表
     *
     * @param page     页面
     * @param pageSize 页面大小
     * @return {@link List<Record>}
     */
    @Override
    public List<Record> list(Long page, Long pageSize) {
        Page<Record> recordPage = new Page<>(page, pageSize);
        return recordMapper.selectPage(recordPage, null).getRecords();
    }
}
