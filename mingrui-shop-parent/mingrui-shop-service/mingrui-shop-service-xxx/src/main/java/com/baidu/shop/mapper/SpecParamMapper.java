package com.baidu.shop.mapper;


import com.baidu.shop.entity.SpecParamEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface SpecParamMapper extends Mapper<SpecParamEntity> {

    @Select(value = "SELECT  s.name FROM tb_spec_param s WHERE id = #{key}")
    String getOwnSpecName(String key);
}
