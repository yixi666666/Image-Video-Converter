package com.yixi.service.impl;

import com.yixi.dto.UserLoginDTO;
import com.yixi.entity.User;
import com.yixi.mapper.UserMapper;
import com.yixi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    public User login(UserLoginDTO userLoginDTO) {
        String accountId = userLoginDTO.getAccountId();
        String password = userLoginDTO.getPassword();
        User user = userMapper.getByAccountId(accountId);

        //账号不存在
        if(user==null){
            return null;
        }
        //密码错误
        if(!password.equals(user.getPassword())){
            return null;
        }
        return user;
    }
}
