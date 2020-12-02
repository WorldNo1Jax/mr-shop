package com.baidu.shop.mapper;

import com.baidu.shop.entity.OrderDetailEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

import java.util.List;

public interface  OrderDetailMapper extends Mapper<OrderDetailEntity>, InsertListMapper<OrderDetailEntity> {

    @Select(value = "SELECT * FROM tb_order_detail WHERE order_id = #{orderId}")
    List<OrderDetailEntity> selectDetailByOrderId(Long orderId);
}
