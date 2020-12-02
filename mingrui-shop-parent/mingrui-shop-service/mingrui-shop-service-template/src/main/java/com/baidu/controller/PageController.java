package com.baidu.controller;

import com.baidu.service.PageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @ClassName PageController
 * @Description: TODO
 * @Author ljc
 * @Date 2020/9/23
 * @Version V1.0
 **/
//@Controller
public class PageController {
    //@Autowired
    private PageService pageService;

    //@GetMapping(value = "/item/{spuId}.html")
    public String test(@PathVariable(value = "spuId") Integer spuId, ModelMap modelMap) {
        Map<String,Object> map = pageService.getPageInfoBySpuId(spuId);

        modelMap.putAll(map);//把一个Map中的键值对存储在另一个Map

        return "item";
    }
}
