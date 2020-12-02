package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.constant.UserConstant;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserMapper;
import com.baidu.shop.redis.RedisRepositroy;

import com.baidu.shop.service.UserService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.BaiduBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName UserServiceImpl
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/13
 * @Version V1.0
 **/
@RestController
@Slf4j
public class UserServiceImpl extends BaseApiService implements UserService {

    @Resource
    private UserMapper userMapper;
    @Autowired
    RedisRepositroy redisRepositroy;

    @Override
    public Result<JSONObject> register(UserDTO userDTO) {

        UserEntity userEntity = BaiduBeanUtil.copyProperties(userDTO, UserEntity.class);
        userEntity.setCreated(new Date());
        //密码加密
        userEntity.setPassword(BCryptUtil.hashpw(userEntity.getPassword(),BCryptUtil.gensalt()));

        userMapper.insertSelective(userEntity);
        return this.setResultSuccess();
    }


    @Override
    public Result<List<UserEntity>> checkUserNameOrPhone(String value, Integer type) {

        Example example = new Example(UserEntity.class);
        Example.Criteria criteria = example.createCriteria();

        if(type == UserConstant.CHECK_TYPE_USERNAME ){
            //验证用户名唯一
            criteria.andEqualTo("username",value);
        }else if (type ==UserConstant.CHECK_TYPE_PHONE){
            //验证手机号唯一
            criteria.andEqualTo("phone",value);
        }

        List<UserEntity> userEntities = userMapper.selectByExample(example);

        return this.setResultSuccess(userEntities);
    }

    @Override
    public Result<JSONObject> sendValidCode(UserDTO userDTO) {

        //生成随机6位验证码
        String code = (int)((Math.random() * 9 + 1) * 100000) + "";
        log.debug("向手机号码:{} 发送验证码:{}",userDTO.getPhone(),code);

        //发送手机验证码  免费的只有十条 就不浪费了
        //LuosimaoDuanxinUtil.SendCode(userDTO.getPhone(),code);

        // 将手机验证码发送到Redis
        redisRepositroy.set(UserConstant.CHECK_PHONE_CODE +userDTO.getPhone(),code);
        // 设置 手机验证码在Redis的存储时间    间隔：秒
        redisRepositroy.expire(UserConstant.CHECK_PHONE_CODE+ userDTO.getPhone(),120);

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> checkValidCode(String phone, String validCode) {

        String s = redisRepositroy.get(UserConstant.CHECK_PHONE_CODE + phone);

        if(!validCode.equals(s)) return this.setResultError(HTTPStatus.CHECK_PHONE_CODE,"验证码校对失败");

        return this.setResultSuccess();
    }
}
