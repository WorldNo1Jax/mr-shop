package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "规格参数接口")
public interface SpecificationService {

    @ApiOperation(value = "分类信息查询")
    @GetMapping(value = "specGroup/getSpecGroupInfo")
    Result<List<SpecGroupEntity>> getSpecGroupInfo(@SpringQueryMap SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "新增规格组")
    @PostMapping(value = "specGroup/saveSpecGroupInfo")
    Result<JSONObject> saveSpecGroupInfo(@Validated(MingruiOperation.Add.class) @RequestBody SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "修改规格组")
    @PutMapping(value = "specGroup/saveSpecGroupInfo")
    Result<JSONObject> editSpecGroupInfo(@Validated(MingruiOperation.Update.class) @RequestBody SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "删除规格组")
    @DeleteMapping(value = "specGroup/delete")
    Result<JSONObject> delete(Integer id);

    @ApiOperation(value = "查询规格参数信息")
    @GetMapping(value = "sepcParam/getSpecParamInfo")
    Result<List<SpecParamEntity>> getSpecParamInfo(@SpringQueryMap SpecParamDTO specParamDTO);

    @ApiOperation(value = "新增规格参数信息")
    @PostMapping(value = "sepcParam/saveSpecParamInfo")
    Result<JsonObject> saveSpecParamInfo(@Validated(MingruiOperation.Add.class) @RequestBody SpecParamDTO specParamDTO);

    @ApiOperation(value = "新增规格参数信息")
    @PutMapping(value = "sepcParam/saveSpecParamInfo")
    Result<JsonObject> editSpecParamInfo(@Validated(MingruiOperation.Update.class) @RequestBody SpecParamDTO specParamDTO);

    @ApiOperation(value = "删除规格参数信息")
    @DeleteMapping(value = "sepcParam/del")
    Result<JsonObject> deleteParam(Integer id);

//    @ApiOperation(value = "通过分类Id 查询规格参数信息")
//    @GetMapping(value = "sepcParam/getSpecByCid")
//    Result<JsonObject> getSpecByCid(Integer cid);

    @ApiOperation("通过规格参数Id查找规格名字")
    @GetMapping(value = "sepcParam/getOwnSpecName")
    String getOwnSpecName(@RequestParam String key);




}
