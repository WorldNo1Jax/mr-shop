package com.baidu.shop.mapper;

import com.baidu.shop.entity.Stock;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ClassName SaleMapper
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/27
 * @Version V1.0
 **/
public interface StockMapper extends Mapper<Stock> {

    @Update(value = "update stock  set sale = sale + 1,version = version + 1 WHERE id = #{id} AND version = #{version}")
    int updateStockByOptimistic(Integer id ,Integer version);

    @Select(value = "select * from stock where id = #{sid} FOR UPDATE")
    Stock getStockByIdForUpdate(int sid);
}
