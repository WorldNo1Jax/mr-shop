package com.baidu.shop.fegin;


import com.baidu.shop.base.Result;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.service.SpecificationService;
import org.json.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "GoodsService",value = "xxx-service")
public interface StockFegin  extends GoodsService{


}
