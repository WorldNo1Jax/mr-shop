package com.baidu.shop.entity;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ClassName StockEntity
 * @Description: TODO
 * @Author jlz
 * @Date 2020-10-27 14:48
 * @Version V1.0
 **/
@Data
@Table(name = "stock")
public class StockEntity {

    @Id
    private Long id;

    private Integer count;

    private Integer sale;

    private Integer version;
}
