package com.baidu.shop.mapper;

import com.baidu.shop.entity.StockEntity;
import com.baidu.shop.service.MiaoService;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface MiaoMapper extends Mapper<StockEntity> {

    @Select(value = "SELECT * FROM tb_stock s WHERE s.seckill_stock > 0\n")
    List<StockEntity> getMiaoOrder();
}
