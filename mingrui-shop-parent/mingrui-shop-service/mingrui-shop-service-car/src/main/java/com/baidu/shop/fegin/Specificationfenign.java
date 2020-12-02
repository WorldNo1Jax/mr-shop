package com.baidu.shop.fegin;

import com.baidu.shop.service.SpecificationService;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(contextId = "SpecificationService",value = "xxx-service")
public interface Specificationfenign extends SpecificationService {
}
