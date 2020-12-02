package com.baidu.shop.feign;


import com.baidu.shop.service.TemplateService;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "tempalate-server",contextId = "TemplateService")
public interface TemplateFeg extends TemplateService {

}
