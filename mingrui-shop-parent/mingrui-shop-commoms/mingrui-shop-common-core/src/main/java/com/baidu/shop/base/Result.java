package com.baidu.shop.base;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName Result
 * @Description: 统一返回
 * @Author ljc
 * @Date 2020/8/17
 * @Version V1.0
 **/
@Data      //提供get set 方法
@NoArgsConstructor  //提供不带参数的构造函数
public class Result<T> {

    private Integer code;//返回码

    private String message;//返回消息

    private T data;//返回数据

    public Result(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = (T) data;
    }

}
