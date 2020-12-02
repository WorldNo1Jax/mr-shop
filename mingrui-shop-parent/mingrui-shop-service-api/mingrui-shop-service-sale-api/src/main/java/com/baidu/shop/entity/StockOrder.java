package com.baidu.shop.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @ClassName OrderEntity
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/27
 * @Version V1.0
 **/
@Table(name = "stock_order")
@Data
@ApiModel(value = "订单类")
public class StockOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //新增返回主键
    private Integer id;

    private  Integer sid;   //购买商品编号

    private  String name;

    private Date create_time;

    private Integer userId;


}
