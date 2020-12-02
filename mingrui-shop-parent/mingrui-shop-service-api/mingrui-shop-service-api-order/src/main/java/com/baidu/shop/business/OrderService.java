package com.baidu.shop.business;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.OrderDTO;
import com.baidu.shop.dto.OrderInfo;
import com.baidu.shop.entity.QueryEntity;
import com.baidu.shop.validate.group.MingruiOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.json.JSONObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "订单接口")
public interface OrderService {

    @ApiOperation("创建订单")
    @PostMapping("order/createOrder")
    Result<String> createOrder(@Validated(value = {MingruiOperation.Add.class}) @RequestBody OrderDTO orderDTO,
                             @CookieValue("MRSHOP_TOKEN") String token);

    @ApiOperation("根据订单id查询订单信息")
    @GetMapping(value = "order/getOrderInfoByOrderId")
    Result<OrderInfo> getOrderInfoByOrderId(@RequestParam Long orderId);

    @ApiOperation("根据用户id查询订单信息")
    @GetMapping(value = "order/getOrderInfoByUserId")
    Result<List<OrderInfo>> getOrderInfoByUserId(QueryEntity queryEntity);

    @GetMapping(value = "order/getSpuIdByskuId")
    Result<JSONObject> getSpuIdByskuId(@RequestParam Long skuId);
}
