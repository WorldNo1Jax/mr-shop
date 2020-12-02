package com.baidu.shop.service;


import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(tags = "模板接口")
public interface TemplateService {

    @ApiOperation(value = "通过spuID动态创建模板")
    @GetMapping(value = "template/createStaticHTMLTemplate")
    Result<JSONObject> createStaticHTMLTemplate(@RequestParam Integer spuId);

    @ApiOperation(value = "初始化静态HTML文件")
    @GetMapping(value = "template/initStaticHTMLTemplate")
    Result<JSONObject> initStaticHTMLTemplate();

    @ApiOperation(value = "删除静态HTML文件")
    @DeleteMapping
    Result<JSONObject> deleteStaticHTMLTemplate(Integer spuId);


}
