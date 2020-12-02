package com.baidu.shop.mapper;


import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<BrandEntity> , SelectByIdListMapper<BrandEntity,Integer> {

    @Select(value = "SELECT * FROM tb_brand b ,tb_category_brand cb WHERE b.id = cb.brand_id AND cb.category_id = #{cid} \n")
    List<BrandEntity> getBrendBycid(Integer cid);
}
