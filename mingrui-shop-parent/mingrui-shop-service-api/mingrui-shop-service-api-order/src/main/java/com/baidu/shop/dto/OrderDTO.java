package com.baidu.shop.dto;

import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @ClassName OrderDTO
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/21
 * @Version V1.0
 **/
@Data
@ApiModel(value = "订单数据传输")
public class OrderDTO {

    @ApiModelProperty(value = "收货地址Id",example = "1")
    @NotNull(message = "收货地址Id不能为空",groups = {MingruiOperation.Update.class})
    private Long addrId;

    @ApiModelProperty(value = "支付方式",example = "1")
    @NotNull(message = "支付方式类型不能为空",groups = {MingruiOperation.Update.class})
    private Integer payType;

    @ApiModelProperty("购买商品Id集合")
    @NotEmpty(message = "购买商品不能为空",groups = {MingruiOperation.Update.class})
    private String skuIds;

}
