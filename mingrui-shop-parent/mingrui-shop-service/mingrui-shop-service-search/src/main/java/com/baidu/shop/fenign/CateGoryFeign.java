package com.baidu.shop.fenign;

import com.baidu.shop.service.CategoryService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "xxx-service",contextId = "CategoryService")
public interface CateGoryFeign extends CategoryService {


}
