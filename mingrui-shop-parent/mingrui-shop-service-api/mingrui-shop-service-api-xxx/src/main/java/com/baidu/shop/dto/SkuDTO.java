package com.baidu.shop.dto;

import com.sun.org.apache.xpath.internal.operations.Bool;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName SkuDTO
 * @Description: TODO
 * @Author ljc
 * @Date 2020/9/8
 * @Version V1.0
 **/
@ApiModel(value = "sku数据传输类")
@Data
public class SkuDTO {

    @ApiModelProperty(value = "主键",example = "1")
    private Long id;

    @ApiModelProperty(value = "spu主键",example = "1")
    private Integer spuId;

    @ApiModelProperty(value = "商品标题")
    private String title;

    @ApiModelProperty(value = "商品的图片,多个图片以‘，’分割")
    private String images;

    @ApiModelProperty(value = "销售的价格,单位以分为单位",example = "1")
    private Integer price;

    @ApiModelProperty(value = "特有规格在Spu属性模板下对应的下标组合")
    private String indexes;

    @ApiModelProperty(value = "sku的特有的规格参数键值对,json格式,反序列化时请使用linkedHashMap，保证有序")
    private String ownSpec;

    @ApiModelProperty(value = "费师傅有效,0无效, 1有效",example = "1")
    private Boolean enable;

    @ApiModelProperty(value = "添加时间")
    private Date createTime;

    @ApiModelProperty(value = "最后的修改时间")
    private Date lastUpdateTime;

    @ApiModelProperty(value = "库存",example = "1")
    private Integer stock;


}
