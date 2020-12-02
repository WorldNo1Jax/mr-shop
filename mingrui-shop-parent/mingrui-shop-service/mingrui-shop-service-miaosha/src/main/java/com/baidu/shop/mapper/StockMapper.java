package com.baidu.shop.mapper;

import com.baidu.shop.entity.StockEntity;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ClassName StockMapper
 * @Description: TODO
 * @Author jlz
 * @Date 2020-10-29 20:12
 * @Version V1.0
 **/
public interface StockMapper  extends Mapper<StockEntity> {

//    @Update(value = " update stock\n" +
//            "    set\n" +
//            "      sale = sale + 1\n" +
//            "   \n" +
//            "    WHERE id = #{id}\n" +
//            "    AND sale = #{sale}")
//    int updateStockByOptimistic(Long id, Integer sale);


    @Update(value = "update stock  set sale = sale + 1,version = version + 1 WHERE id = #{id} AND version = #{version} " +
            "AND (count - sale) >0")
    int updateStockByOptimistic(Long id, Integer version);

}
