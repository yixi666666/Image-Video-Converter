package com.yixi.controller;

import com.yixi.dto.UserLoginDTO;
import com.yixi.entity.User;
import com.yixi.result.Result;
import com.yixi.service.UserService;
import com.yixi.vo.UserLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userDTO) {
        User user = userService.login(userDTO);
        if (user == null) {
            return Result.error("账号或密码错误！");
        }
        log.info("密码正确");
        //返回了账号，密码正确
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .accountId(user.getAccountId())
                .userName(user.getUsername())
                .avatar(user.getAvatar())
                .build();

        return Result.success(userLoginVO);
    }

}
