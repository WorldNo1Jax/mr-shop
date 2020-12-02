package com.baidu.shop.status;

public class HTTPStatus {

    public static final int OK = 200;//成功

    public static final int ERROR = 500;//失败

    public static final int OPERATION_ERROR = 5001;//操作失败

    public static final int PARAMS_VALIDATE_ERROR = 5002;//参数校验失败

    public static final int CHECK_PHONE_CODE = 5003;//验证码校验失败

    public static final int VALID_USER_PASSWORD_ERROR  = 5004;//账号或密码错误

    public static final int VALID_PHONE_ERROR  = 5005;// 手机验证码错误

    public static final int VERIFY_ERROR  = 5006;//用户失效



}
