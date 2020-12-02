package com.baidu.shop.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @ClassName SpuEntity
 * @Description: TODO
 * @Author ljc
 * @Date 2020/9/7
 * @Version V1.0
 **/
@Data
@Table(name = "tb_spu")
public class SpuEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //新增返回主键
    private Integer id;

    private String title;

    private String subTitle;

    private Integer cid1;

    private Integer cid2;

    private Integer cid3;

    private Integer brandId;

    private Integer saleable;

    private Integer valid;

    private Date createTime;

    private Date lastUpdateTime;
}
