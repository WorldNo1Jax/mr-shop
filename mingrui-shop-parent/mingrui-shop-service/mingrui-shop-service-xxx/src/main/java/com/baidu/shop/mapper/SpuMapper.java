package com.baidu.shop.mapper;

import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.validation.annotation.Validated;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpuMapper extends Mapper<SpuEntity> {

    List<SpuDTO> list(SpuDTO spuDTO);

    Integer count(SpuDTO spuDTO);

    @Select(value = "SELECT  * FROM tb_spu s WHERE s.brand_id = #{brandId}")
    List<SpuEntity> getSpuByBrandId(Integer brandId);
}
