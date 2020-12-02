package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @ClassName UserService
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/13
 * @Version V1.0
 **/
@Api(tags = "注册接口")
public interface UserService {

    @ApiOperation(value = "用户注册")
    @PostMapping("user/register")
    Result<JSONObject> register(@Validated({MingruiOperation.Add.class}) @RequestBody UserDTO userDTO);

    @ApiOperation(value = "检验用户名或手机号唯一")
    @GetMapping("user/check/{value}/{type}")
    Result<List<UserEntity>> checkUserNameOrPhone(@PathVariable(value = "value") String value,
                                                  @PathVariable(value = "type") Integer type);

    @ApiOperation(value = "给手机号发送短新")
    @PostMapping("user/sendValidCode")
    Result<JSONObject> sendValidCode(@RequestBody UserDTO userDTO);

    @ApiOperation(value = "验证验证码是否一致")
    @GetMapping("user/checkCode")
    Result<JSONObject> checkValidCode(String phone,String validCode);
}
