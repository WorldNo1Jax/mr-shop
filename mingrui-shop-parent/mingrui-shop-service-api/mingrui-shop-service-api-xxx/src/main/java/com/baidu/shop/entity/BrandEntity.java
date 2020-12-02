package com.baidu.shop.entity;

import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Table(name = "tb_brand")
@Data
@ApiModel(value = "品牌实体类")
public class BrandEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //新增返回主键
    private Integer id;

    private String name;

    private String image;

    private Character letter;

}
