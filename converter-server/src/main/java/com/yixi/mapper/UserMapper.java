package com.yixi.mapper;

import com.yixi.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据账号id查询用户
     * @param accountId
     * @return
     */
    @Select("select * from user where account_id = #{accountId}")
    User getByAccountId(String accountId);
}
