package com.baidu.shop.mapper;

import com.baidu.shop.entity.StockEntity;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.additional.idlist.DeleteByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.Map;

public interface StockMapper extends Mapper<StockEntity>, DeleteByIdListMapper<StockEntity,Long> {


    @Update("UPDATE tb_stock set stock = stock -#{num} WHERE sku_id = #{skuId}")
    void updateByskuIdAndNum(Long skuId, Integer num);
}
