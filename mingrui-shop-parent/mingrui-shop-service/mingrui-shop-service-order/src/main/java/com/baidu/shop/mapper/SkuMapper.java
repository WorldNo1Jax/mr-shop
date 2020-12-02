package com.baidu.shop.mapper;

import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.entity.SkuEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.DeleteByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuMapper extends Mapper<SkuEntity>, DeleteByIdListMapper<SkuEntity,Long> {


    @Select(value = "SELECT spu_id FROM tb_sku WHERE id = #{skuId}")
    Integer getSpuIdByskuId(Long skuId);
}
