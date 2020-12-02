package com.baidu.shop.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName SaleEntity
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/27
 * @Version V1.0
 **/
@Table(name = "stock")
@Data
@ApiModel(value = "库存类")
public class Stock {

    @Id
    private Integer id ;

    private String name;

    private  Integer count; //库存

    private  Integer sale;  //已售

    private  Integer version;

}
