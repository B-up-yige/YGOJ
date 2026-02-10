package com.ygoj.user.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NonNull;

@Data
public class Userinfo {
    @TableId(type = IdType.AUTO)
    Long id;
    String username;
    String nickname;
    String password;
    String email;
}
