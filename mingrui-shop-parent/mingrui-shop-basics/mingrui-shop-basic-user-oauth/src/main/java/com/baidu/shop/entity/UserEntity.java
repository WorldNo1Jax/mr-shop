package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @ClassName UserEntity
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/13
 * @Version V1.0
 **/
@Table(name = "tb_user")
@Data
public class UserEntity {
    @Id
    private Integer id;
    // 用户名
    private String username;
    // 密码
    private String password;
    // 手机号
    private String phone;
    // 用户的创建时间
    private Date created;
    // 盐
    private String salt;


}
