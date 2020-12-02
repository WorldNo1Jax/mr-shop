package com.baidu.shop.service;


import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "品牌接口")
public interface BrandService {

    @ApiOperation(value = "获取品牌信息")
    @GetMapping(value = "brand/getBrandInfo")
    Result<PageInfo<BrandEntity>> getBrandInfo(@SpringQueryMap BrandDTO brandDTO);

    @ApiOperation(value = "新增品牌信息")
    @PostMapping(value = "brand/saveBrandInfo")
    Result<JsonObject> saveBrandInfo(@Validated({MingruiOperation.Add.class}) @RequestBody BrandDTO brandDTO);

    @ApiOperation(value = "修改品牌信息")
    @PutMapping(value = "brand/saveBrandInfo")
    Result<JsonObject> editBrandInfo(@Validated({MingruiOperation.Update.class}) @RequestBody BrandDTO brandDTO);

    @ApiOperation(value = "删除品牌信息")
    @DeleteMapping(value = "brand/delete")
    Result<JsonObject> deleteBrand(Integer brandId);

    @ApiOperation(value = "通过cid获取品牌信息")
    @GetMapping(value = "brand/getBrandByCid")
    Result<BrandEntity> getBrandByCid(Integer cid);

    @ApiOperation(value = "通过品牌Id集合获取品牌信息")
    @GetMapping(value = "brand/getByBrandIdList")
    Result<List<BrandEntity>> getByBrandIdList(@RequestParam String brandsStr);
}
