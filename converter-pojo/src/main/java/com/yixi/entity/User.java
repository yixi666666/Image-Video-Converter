package com.yixi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private static final long serialVersionUID = 1L;

    private Long id;            //主键
    private String accountId;   //账号
    private String username;    //用户名
    private String password;    //密码
    private LocalDateTime createTime;  //创建时间
    private String avatar;      //头像

}
