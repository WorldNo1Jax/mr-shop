package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @ClassName LogEntity
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/30
 * @Version V1.0
 **/
@Data
@Table(name = "tb_log")
public class LogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //主键

    private Long userId;//用户id

    private String userName;//用户name

    private String operation;//用户的操作

    private String operation_method;//用户操作的方法

    private String ip; //用户登录的ip地址

    private Date operationTime;//用户操作的时间

    private String model;//用户操作的模块

    private String result;//返回的数据

    private String type;//操作的类型

    private String params;//传的值
}
