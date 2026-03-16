package com.ygoj.record;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Record {
    @TableId(type = IdType.AUTO)
    Long id;
    Long userId;
    Long problemId;
    String status;
}
