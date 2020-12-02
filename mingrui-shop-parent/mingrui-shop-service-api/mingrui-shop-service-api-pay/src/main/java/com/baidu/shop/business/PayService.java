package com.baidu.shop.business;

import com.baidu.shop.dto.PayinfoDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(tags = "支付接口")
public interface PayService {

    @ApiModelProperty("请求支付")
    @GetMapping("pay/requestPay")
    void requestPay(PayinfoDTO payinfoDTO, @CookieValue("MRSHOP_TOKEN") String token,
                        HttpServletResponse httpServletResponse);

    @ApiModelProperty("接收支付宝通知")
    @GetMapping("pay/returnNotify")
    void returnNotify(HttpServletRequest httpServletRequest);

    @ApiModelProperty("返回支付成功页面")
    @GetMapping("pay/returnURL")
    void returnURL(HttpServletRequest request,HttpServletResponse httpServletResponse);
}
