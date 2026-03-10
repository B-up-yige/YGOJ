package com.ygoj.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Userinfo {
    @TableId(type = IdType.AUTO)
    Long id;
    String username;
    String nickname;
    String password;
    String email;
}
