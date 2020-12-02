package com.baidu.shop.aspect;

import com.baidu.shop.comment.MyLog;
import com.baidu.shop.config.GetIP;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.LogEntity;
import com.baidu.shop.mapper.LogMapper;
import com.baidu.shop.utils.CookieUtils;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.JwtUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * @ClassName MyLogAspect
 * @Description: 切面处理类
 * @Author ljc
 * @Date 2020/10/30
 * @Version V1.0
 **/

@Aspect   // @Aspect的意思是面向切面编程，一个类前面加上@Aspect说明这个类使用了这个技术
@Component
public class MyLogAspect {

    @Resource
    private JwtConfig jwtConfig;

    @Resource
    private LogMapper logMapper;

    /**
     * 设置操作日志切入点   在注解的位置切入代码
     */
    @Pointcut("@annotation(com.baidu.shop.comment.MyLog)")
    public void logPointCut(){}

    /**
     *
     * @param joinPoint 方法的执行点
     * @param result 方法的返回值
     */
    /**
     * @AfterReturning(
     *      value="切入点表达式或命名切入点",
     *      pointcut="切入点表达式或命名切入点",
     *      argNames="参数列表参数名",
     *      returning="返回值对应参数名")
     */
    @Transactional
    @AfterReturning(returning = "result",pointcut = "logPointCut()")
    public void  saveRecordlog(JoinPoint joinPoint,Object result){

        /**
         * joinPoint对象 中封装了SpringAop中的属性
         */

        //获取 RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        //从 RequestAttributes 获得 HttpServletRequest
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        System.out.println("request: "+request);

        //从切面织入点通过反射获取织入点的方法
        // 获取封装了署名信息的对象,在该对象中可以获取到目标方法名,所属类的Class等信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // 下面两个数组中，参数值和参数名的个数和位置是一一对应的。
        String[] parameterNames = signature.getParameterNames();  //获取参数名
        Object[] args = joinPoint.getArgs();   //获取参数值

        //用map存储 参数名和 参数值
        HashMap<Object, Object> map = new HashMap<>();
        for (int i = 0; i< parameterNames.length ; i++){
            if(!parameterNames[i].equals("token"))
                map.put(parameterNames[i],args[i]);
        }
        ArrayList<Object> objects = new ArrayList<>();

        //获取到切入点所在的方法
        Method method = signature.getMethod();

        //给 logEntity 赋值
        LogEntity logEntity = new LogEntity();

        System.out.println("map: "+JSONUtil.toJsonString(map));
        logEntity.setParams(JSONUtil.toJsonString(map)); //方法传递的参数 :值
        //通过工具类从 Request中获取ip地址
        logEntity.setIp(GetIP.getIpAddress(request)); // 用户登录的ip地址
        logEntity.setOperationTime(new Date());  //操作时间
        logEntity.setOperation_method(request.getRequestURI()); // 访问的方法


        //获取方法的值
        MyLog annotation = method.getAnnotation(MyLog.class);
        if(null!=annotation){
            logEntity.setOperation(annotation.operation());//具体的操作
            logEntity.setModel(annotation.operationModel()); //操作的模块
            logEntity.setType(annotation.operationType());  //操作的类型
        }

        String cookieValue = CookieUtils.getCookieValue(request, jwtConfig.getCookieName());
        if ( null!= cookieValue ) {
            try {
                UserInfo userInfo = JwtUtils.getInfoFromToken(cookieValue, jwtConfig.getPublicKey());
                logEntity.setUserId(userInfo.getId().longValue());  //用户ID
                logEntity.setUserName(userInfo.getUsername()); //用户名字
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        logMapper.insertSelective(logEntity);

    }

}
