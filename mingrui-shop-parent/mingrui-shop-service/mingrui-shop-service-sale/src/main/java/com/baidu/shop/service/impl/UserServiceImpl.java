package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.mapper.UserMapper;
import com.baidu.shop.service.UserService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName UserServiceImpl
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/28
 * @Version V1.0
 **/
@RestController
public class UserServiceImpl extends BaseApiService implements UserService {
    @Resource
    private UserMapper userMapper;




}
