package com.baidu.shop.business;

import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserDTO;


public interface UserOauthService {

    String checkUser(UserDTO userDTO, JwtConfig jwtConfig);

}
