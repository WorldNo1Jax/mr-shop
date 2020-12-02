package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @ClassName OrderEntity
 * @Description: TODO
 * @Author jlz
 * @Date 2020-10-27 14:52
 * @Version V1.0
 **/
@Data
@Table(name = "stock_order")
public class OrderEntity {

    @Id
    private Long id;

    private Integer userId;

    private Long stockId;

    private Date createTime;
}
