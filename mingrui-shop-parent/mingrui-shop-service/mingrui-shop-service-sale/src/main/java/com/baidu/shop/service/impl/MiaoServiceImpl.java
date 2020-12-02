package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.entity.StockEntity;
import com.baidu.shop.mapper.MiaoMapper;
import com.baidu.shop.service.MiaoService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName MiaoServiceImpl
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/30
 * @Version V1.0
 **/

@RestController
public class MiaoServiceImpl extends BaseApiService implements MiaoService {

    @Resource
    private MiaoMapper miaoMapper;



    @Override
    public Result<JSONObject> getMiaoOrderData() {

        //查询 可秒杀库存大于0 的商品集合 返回库存信息
        List<StockEntity> stockList =  miaoMapper.getMiaoOrder();



        return this.setResultSuccess(stockList);
    }
}
