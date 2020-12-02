package com.baidu.shop.web;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.UserOauthService;

import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.CookieUtils;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName UserOauthController
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/15
 * @Version V1.0
 **/
@RestController
@Api(tags = "用户登录接口")
public class UserOauthController extends BaseApiService {

    @Autowired
    private UserOauthService userOauthService;
    @Autowired
    private JwtConfig jwtConfig;


    @PostMapping(value = "oauth/login")
    @ApiOperation(value = "登录")
    public Result<JSONObject> login(@RequestBody UserDTO userDTO,
                    HttpServletRequest request, HttpServletResponse response){

        String token = null;

        if(StringUtil.isNotEmpty(userDTO.getPhone())){
            //  通过手机号验证 --》 比较验证码 --》 创建cookie
            //获得 token
            token = userOauthService.checkUser(userDTO,jwtConfig);
            System.out.println("Phone :"+token);

        }else{ //  通过用户查找用户信息是否正确  -->> 比较密码  -->>创建cookie

            //获得token
            token = userOauthService.checkUser(userDTO,jwtConfig);
            System.out.println(token);

        }

        //判断token是否为null
        if(ObjectUtil.isNull(token)){
            if(StringUtil.isNotEmpty(userDTO.getPhone())){
                return this.setResultError(HTTPStatus.VALID_PHONE_ERROR,"手机号验证码错误");
            }
            return this.setResultError(HTTPStatus.VALID_USER_PASSWORD_ERROR,"账号或密码错误");
        }

        //将token放到cookie中
        CookieUtils.setCookie(request,response,jwtConfig.getCookieName(),token,jwtConfig.getCookieMaxAge(),true);

        return this.setResultSuccess();
    }


    //验证登录状态
    @GetMapping("oauth/verify")
    public Result<UserInfo> checkUserIsLogin(@CookieValue(value = "MRSHOP_TOKEN") String token,
                                             HttpServletRequest request, HttpServletResponse response){

        try {
            //通过公钥得到 userInfo
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            //可以解析token 用户处于登录状态  刷新token
            token  = JwtUtils.generateToken(userInfo,jwtConfig.getPrivateKey(),jwtConfig.getExpire());

            //放入cookie中  token时间延长
            CookieUtils.setCookie(request,response,jwtConfig.getCookieName(),token,jwtConfig.getCookieMaxAge(),true);

            return this.setResultSuccess(userInfo);
        } catch (Exception e) { //如果有异常 说明token失效
            e.printStackTrace();

        }

        return this.setResultError(HTTPStatus.VERIFY_ERROR,"用户失效403");
    }

}
