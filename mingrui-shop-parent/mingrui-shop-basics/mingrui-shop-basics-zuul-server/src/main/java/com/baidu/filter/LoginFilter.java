package com.baidu.filter;


import com.baidu.config.JwtConfig;
import com.baidu.config.JwtConfig;
import com.baidu.shop.utils.CookieUtils;
import com.baidu.shop.utils.JwtUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;

/**
 * @ClassName LoginFilter
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/17
 * @Version V1.0
 **/
@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtConfig jwtConfig;

    private static final Logger logger = LoggerFactory.getLogger(LoginFilter.class);

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;   //前缀拦截
    }

    @Override
    public int filterOrder() {
        return 5;   //优先级不能设置太高，
    }


    @Override
    public boolean shouldFilter() {

        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = ctx.getRequest();
        //获取URI
        String uri = request.getRequestURI();
        logger.debug("==============" +uri);
        // 如果当前请求是不拦截请求， 拦截器不生效
        System.out.println(!jwtConfig.getExcludePath().contains(uri));
        return !jwtConfig.getExcludePath().contains(uri);
    }

    @Override
    public Object run() throws ZuulException {

        //获取上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = context.getRequest();
        logger.info("拦截到请求：" +request.getRequestURI());
        //获取token
        String token = CookieUtils.getCookieValue(request, jwtConfig.getCookieName());
        logger.info("token信息"+token);

        if (null!=token){
            // 校验
            try {
                // 通过公钥解密，如果成功，就放行，失败就拦截
                JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            } catch (Exception e) {
                logger.info("token "+token);
                // 校验出现异常，返回403
                context.setSendZuulResponse(false);
                context.setResponseStatusCode(403);
            }
        }else {
            logger.info("没有token");
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(403);
        }

        return null;

    }
}
