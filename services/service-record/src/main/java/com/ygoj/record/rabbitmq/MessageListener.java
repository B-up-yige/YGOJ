package com.ygoj.record.rabbitmq;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rabbitmq.client.Channel;
import com.ygoj.judger.ExecuteDetail;
import com.ygoj.judger.sandbox.SandboxExecuteResponse;
import com.ygoj.record.RecordDetail;
import com.ygoj.record.mapper.RecordDetailMapper;
import com.ygoj.record.mapper.RecordMapper;
import lombok.SneakyThrows;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.ygoj.record.Record;

@Component
public class MessageListener {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RecordDetailMapper recordDetailMapper;
    @Autowired
    private RecordMapper recordMapper;

    @GlobalTransactional
    @SneakyThrows
    @RabbitListener(queues = {"judgeResult_queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        //json对象化
        SandboxExecuteResponse sandboxExecuteResponse = JSON.parseObject(message, SandboxExecuteResponse.class);

        //添加测试样例结果到数据库
        for(ExecuteDetail detail : sandboxExecuteResponse.getDetail()){
            RecordDetail recordDetail = new RecordDetail();
            recordDetail.setRecordId(sandboxExecuteResponse.getRecordId());
            recordDetail.setTime(detail.getTime());
            recordDetail.setMemory(detail.getMemory());
            recordDetail.setStatus(detail.getStatus());

            recordDetailMapper.insert(recordDetail);
        }

        //更新测试结果
        LambdaQueryWrapper<Record> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Record::getId, sandboxExecuteResponse.getRecordId());
        Record record = recordMapper.selectOne(queryWrapper);
        record.setStatus(sandboxExecuteResponse.getStatus());
        recordMapper.update(record, queryWrapper);

        //确认消息
        channel.basicAck(deliveryTag, false);
    }
}
