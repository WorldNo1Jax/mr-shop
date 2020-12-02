package com.baidu.shop.feign;

import com.baidu.shop.service.ShopElasticsearchService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ClassName SearchFeign
 * @Description: TODO
 * @Author ljc
 * @Date 2020/9/27
 * @Version V1.0
 **/
@FeignClient(value = "search-server",contextId = "SearchFeign")
public interface SearchFeign extends ShopElasticsearchService {
}
