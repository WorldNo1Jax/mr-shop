package com.baidu.shop.base;


import com.baidu.shop.utils.ObjectUtil;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;



@Data
@ApiModel(value = "BaseDTO用于数据传输，其他DTO需要继承此类")
public class BaseDTO {

    @ApiModelProperty(value = "当前页", example = "1")
    private Integer page;

    @ApiModelProperty(value = "每页显示多少条",example = "5")
    private Integer rows;

    @ApiModelProperty(value = "排序字段")
    private String sort;

    @ApiModelProperty(value = "是否升序")
    private Boolean order;

    public String getOrderByClause(){

        if(ObjectUtil.isNotNull(order))  return (sort +" "+ (order?"desc":"asc"));

        return null;
    }

}
