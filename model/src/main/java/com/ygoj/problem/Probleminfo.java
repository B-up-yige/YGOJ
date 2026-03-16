package com.ygoj.problem;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.ygoj.user.Userinfo;
import lombok.Data;

@Data
public class Probleminfo {
    @TableId(type = IdType.AUTO)
    Long id;
    String title;
    String description;
    Long authorId;
    @TableField(exist = false)
    Userinfo author;
    Long timeLimit;
    Long memoryLimit;
}
