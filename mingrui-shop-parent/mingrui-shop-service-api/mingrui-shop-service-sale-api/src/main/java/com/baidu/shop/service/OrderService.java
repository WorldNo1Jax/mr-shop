package com.baidu.shop.service;


import org.springframework.web.bind.annotation.*;

public interface OrderService {



    //创建正确订单：验证库存 + 下单乐观锁 + 更新订单信息到缓存
    public void createOrderByMq(Integer sid, Integer userId);

     //下单接口：异步处理订单
    @GetMapping(value = "/createUserOrderWithMq")
    public String createUserOrderWithMq(@RequestParam(value = "sid") Integer sid,
                                        @RequestParam(value = "userId") Integer userId);
}
