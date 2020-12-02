package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.DetailDTO;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SkuEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.entity.SpuEntity;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品接口")
public interface GoodsService {

    @ApiOperation(value = "获取spu信息")
    @GetMapping(value = "goods/getSpuInfo")
    Result<List<SpuDTO>> getSpuInfo(@SpringQueryMap SpuDTO spuDTO);

    @ApiOperation(value = "新增商品")
    @PostMapping(value = "goods/saveGoods")
    Result<JsonObject> saveGoods(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "根据spuid 获得spudetail数据 ")
    @GetMapping(value = "goods/getDetailBySpuId")
    Result<SpuDetailEntity> getDetailBySpuId(@RequestParam Integer spuId);

    @ApiOperation(value = "根据spuid 获得sku数据 ")
    @GetMapping(value = "goods/getSkuBySpuId")
    Result<List<SkuDTO>> getSkuBySpuId(@RequestParam Integer spuId);

    @ApiOperation(value = "根据skuid 获得sku数据 ")
    @GetMapping(value = "goods/getSkuById")
    Result<SkuEntity> getSkuById(@RequestParam Long skuId);

    @ApiOperation(value = "修改商品")
    @PutMapping(value = "goods/saveGoods")
    Result<JsonObject> editInfo(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "删除商品")
    @DeleteMapping(value = "goods/deleteGoods")
    Result<JsonObject> deleteGoods(Integer spuId);

    @ApiOperation(value = "上架和下架")
    @PutMapping(value = "goods/soldOut")
    Result<JsonObject> soldAdd(@RequestBody SpuDTO spuDTO);

    @ApiOperation("通过skuId和商品数量更新库存")
    @PutMapping("goods/getStockByskuId")
    Result<JSONObject> getStockByskuIdAndNum(@RequestBody DetailDTO detailDTO);

}
