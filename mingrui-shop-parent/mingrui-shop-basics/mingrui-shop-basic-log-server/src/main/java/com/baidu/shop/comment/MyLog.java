package com.baidu.shop.comment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @ClassName MyLog
 * @Description:  自定义注解
 * @Author ljc
 * @Date 2020/10/30
 * @Version V1.0
 **/

@Target(ElementType.METHOD) //注解放置的目标位置,METHOD是可注解在方法级别上
@Retention(RetentionPolicy.RUNTIME) //注解哪个阶段执行
public @interface MyLog {


    //定义注解中的属性  default  ""  给默认值
    String operationModel() default  ""; //操作模块

    String operationType() default  ""; //操作类型

    String operation() default  ""; //具体操作



}
