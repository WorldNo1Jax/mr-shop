package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品分类接口")
public interface CategoryService {

    @ApiOperation(value = "查询商品分类")
    @GetMapping(value = "category/list")
    Result<List<CategoryEntity>> getCategoryByPid(Integer pid);

    //新增
    @ApiOperation(value = "新增商品")
    @PostMapping(value = "category/save")
    //声明哪个组下面的参数参加校验-->当前是校验新增组
    Result<JsonObject> saveCategoryByPid(@Validated(MingruiOperation.Add.class) @RequestBody CategoryEntity entity);

    //修改
    @ApiOperation(value = "修改商品")
    @PutMapping(value = "category/edit")
    //声明哪个组下面的参数参加校验-->当前是校验修改组
    Result<JsonObject> editCategoryByPid(@Validated(MingruiOperation.Update.class) @RequestBody CategoryEntity entity);

    //删除
    @ApiOperation(value = "删除商品")
    @DeleteMapping(value = "category/delete")
    Result<JsonObject> DeleteCategoryByPid(Integer Id);

    @ApiOperation(value = "通过品牌Id查询商品分类")
    @GetMapping(value = "category/getByBrandId")
    Result<List<CategoryEntity>> getByBrandId(Integer brandId);

    @ApiOperation(value = "通过分类id集合查询商品分类")
    @GetMapping(value = "category/getCategoryByIdList")
    Result<List<CategoryEntity>> getCategoryByIdList(@RequestParam String cidsStr);


}
