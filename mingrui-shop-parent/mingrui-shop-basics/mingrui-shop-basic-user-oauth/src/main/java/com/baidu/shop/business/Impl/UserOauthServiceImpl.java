package com.baidu.shop.business.Impl;

import com.baidu.shop.business.UserOauthService;
import com.baidu.shop.business.UserService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserOauthMapper;
import com.baidu.shop.utils.*;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName UserOauthService
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/15
 * @Version V1.0
 **/
@Service
public class UserOauthServiceImpl implements UserOauthService {

    @Resource
    private UserOauthMapper userOauthMapper;
    @Resource
    private UserService userService;

    //登录验证
    @Override
    public String checkUser(UserDTO userDTO, JwtConfig jwtConfig) {

        String token = null;

        //条件查询
        Example example = new Example(UserEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtil.isNotEmpty(userDTO.getPhone())){
            //手机号
            criteria.andEqualTo("phone",userDTO.getPhone());
        }else {
            //账号密码
            criteria.andEqualTo("username",userDTO.getUsername());
        }

        List<UserEntity> list = userOauthMapper.selectByExample(example);

        //当账号唯一时
        if(list.size()==1){
            UserEntity entity = list.get(0);

            if(StringUtil.isNotEmpty(userDTO.getPhone())){
                //验证手机号 验证码是否正确
                if(userService.checkValidCode(userDTO.getPhone(),userDTO.getCode()).getCode()==200){
                    //创建token
                    try {
                        token = JwtUtils.generateToken(new UserInfo(entity.getId(),entity.getUsername()),jwtConfig.getPrivateKey(),jwtConfig.getExpire());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }else {
                //比较密码
                if (BCryptUtil.checkpw(userDTO.getPassword(),entity.getPassword())){
                    //创建token
                    try {
                        token = JwtUtils.generateToken(new UserInfo(entity.getId(),entity.getUsername()),jwtConfig.getPrivateKey(),jwtConfig.getExpire());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return token;
    }


}
