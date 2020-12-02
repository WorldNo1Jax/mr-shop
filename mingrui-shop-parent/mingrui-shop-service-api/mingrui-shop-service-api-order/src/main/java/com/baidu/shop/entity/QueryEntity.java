package com.baidu.shop.entity;

import lombok.Data;

/**
 * @ClassName Query
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/29
 * @Version V1.0
 **/
@Data
public class QueryEntity {

    private Long userId; //用户ID

    private Integer status; //付款状态

    private Integer page;

    private Integer rows;
}
