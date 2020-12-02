package com.baidu.shop.service;

import com.alibaba.fastjson.JSON;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.Car;
import com.baidu.shop.dto.CarSkuIdDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName CarService
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/19
 * @Version V1.0
 **/
@Api
public interface CarService {

    @ApiOperation("添加商品到购物车")
    @PostMapping("car/addCar")
    Result<JSONObject> addCar(@RequestBody Car car, @CookieValue("MRSHOP_TOKEN") String token);

    @ApiOperation("合并购物车")
    @PostMapping("car/mergeCar")
    Result<JSONObject> mergeCar(@RequestBody String clientCarList, @CookieValue("MRSHOP_TOKEN") String token);

    @ApiOperation("查询购物车")
    @GetMapping("car/getUserGoodsCar")
    Result<JSONObject> getUserGoodsCar(@CookieValue("MRSHOP_TOKEN") String token);

    @ApiOperation("修改商品在购物车中的数量")
    @GetMapping("car/carNumUpdate")
    Result<JSONObject> carNumUpdate(@RequestParam Long skuId,@RequestParam Integer type,@CookieValue("MRSHOP_TOKEN") String token);

    @ApiOperation("删除购物车中商品,单删")
    @DeleteMapping(value = "car/delCar")
    Result<JSONObject> delCar(Long skuId,@CookieValue("MRSHOP_TOKEN") String token);

    @ApiOperation("删除购物车中商品,多删")
    @PostMapping(value = "car/delCarAll")
    Result<JSONObject> delCarAll(@RequestBody CarSkuIdDTO carSkuIdDTO, @CookieValue("MRSHOP_TOKEN") String token);
}
