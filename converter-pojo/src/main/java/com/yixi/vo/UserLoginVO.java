package com.yixi.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserLoginVO {
    private String accountId;     //账号
    private String userName ;     //用户名
    private String avatar;        //头像
    private String token;
}
