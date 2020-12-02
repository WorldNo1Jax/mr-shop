package com.baidu.shop.fegin;

import com.baidu.shop.base.Result;
import com.baidu.shop.business.OrderService;
import com.baidu.shop.dto.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "OrderService",value = "order-server")
public interface OrderFegin  {

    //此处不能直接继承 OrderService  OrderService中还有一个post方法是两个参数
    // fegin调用post两个参数会报错
    @GetMapping(value = "order/getOrderInfoByOrderId")
    Result<OrderInfo> getOrderInfoByOrderId(@RequestParam Long orderId);



}
