package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.response.GoodsResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName ShopElasticsearchService
 * @Description: TODO
 * @Author ljc
 * @Date 2020/9/16
 * @Version V1.0
 **/
@Api(tags = "es接口")
public interface ShopElasticsearchService {

    //ES数据初始化-->索引创建,映射创建,mysql数据同步
    @ApiOperation(value = "ES商品数据初始化-->索引创建,映射创建,mysql数据同步")
    @GetMapping(value = "es/initGoodsEsData")
    Result<JSONObject> initGoodsEsData();

//    //添加数据到es库
//    @ApiOperation(value = "新增ES数据-->")
//    @PostMapping(value = "es/initGoodsEsData")
//    Result<JSONObject> initGoodEsDate(@RequestParam Integer spuId);

    @ApiOperation(value = "清空ES中的商品数据")
    @GetMapping(value = "es/clearGoodsEsData")
    Result<JSONObject> clearGoodsEsData();

    @ApiOperation(value = "搜索商品")
    @GetMapping(value = "es/search")
    GoodsResponse getSearch(@RequestParam String search , @RequestParam Integer page,@RequestParam String filter);

    @ApiOperation(value = "新增数据到es")
    @PostMapping(value = "es/saveData")
    Result<JSONObject> saveData(Integer spuId);

    @ApiOperation(value = "通过id删除es数据")
    @DeleteMapping(value = "es/saveData")
    Result<JSONObject> delData(Integer spuId);

}
