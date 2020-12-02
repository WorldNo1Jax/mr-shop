package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface MiaoService {

    //查询 可秒杀库存大于0 的商品集合
    @GetMapping("sale/getMiaoOrderData")
    Result<JSONObject> getMiaoOrderData();


}
