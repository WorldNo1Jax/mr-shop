package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName UserEntity
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/28
 * @Version V1.0
 **/
@Data
@Table(name = "user")
public class UserEntity {

    @Id
    private Long id;

    private String userName;
}
