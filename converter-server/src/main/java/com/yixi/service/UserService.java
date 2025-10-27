package com.yixi.service;

import com.yixi.dto.UserLoginDTO;
import com.yixi.entity.User;

public interface UserService {
   User login(UserLoginDTO userLoginDTO);

}
