package com.baidu.shop.mapper;


import com.baidu.shop.entity.OrderEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface OrderMapper extends Mapper<OrderEntity> {


    //查询所有的订单
    @Select(value = "select count(1) FROM tb_order")
    Integer count();

    //根据不同的支付状态查询订单
    @Select(value = "SELECT * FROM tb_order t1,tb_order_status t2 WHERE \n" +
            "            t1.order_id = t2.order_id \n" +
            "             and t1.user_id = 31\n" +
            "            and t2.`status` = 4")
    List<OrderEntity> selectWeifukuan(Long userId, Integer status);

    // 根据不同的支付状态查询订单总条数
    @Select(value = "select count(1) FROM tb_order_status t WHERE t.`status` = #{status}")
    Integer countStauts(Integer status);
}
