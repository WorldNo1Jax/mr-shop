package com.baidu.shop.service;




import org.springframework.web.bind.annotation.*;


public interface SaleService {

    // 乐观锁加版本号
    @GetMapping("/createWrongOrder/{sid}")
    String createWrongOrder(@PathVariable int sid);

    //乐观锁  + 令牌桶算法
    @PostMapping("/createOptimisticOrder/{sid}")
    String createOptimisticOrder(@PathVariable int sid);

    //悲观锁  更新库存
    @PostMapping("/createPessimisticOrder/{sid}")
    String createPessimisticOrder(@PathVariable int sid);

    // 获取验证码
    @GetMapping(value = "/getVerifyHash")
    String getVerifyHash(@RequestParam(value = "sid") Integer sid,
                                @RequestParam(value = "userId") Integer userId);

    //需要验证的抢购接口
    @GetMapping(value = "/createOrderWithVerifiedUrl")
    String createOrderWithVerifiedUrl(@RequestParam(value = "sid") Integer sid,
                                             @RequestParam(value = "userId") Integer userId,
                                             @RequestParam(value = "verifyHash") String verifyHash);

    // 要求验证的抢购接口 + 单用户限制访问频率
    @GetMapping(value = "/createOrderWithVerifiedUrlAndLimit")
    String createOrderWithVerifiedUrlAndLimit(@RequestParam(value = "sid") Integer sid,
                                                     @RequestParam(value = "userId") Integer userId,
                                                     @RequestParam(value = "verifyHash") String verifyHash);

    //查询库存：通过数据库查询库存
    @GetMapping("/getStockByDB/{sid}")
    public String getStockByDB(@PathVariable int sid);

    //查询库存：通过缓存查询库存
    @GetMapping("/getStockByCache/{sid}")
    public Integer getStockByCache(@PathVariable int sid);

    //  下单接口：先更新数据库，再删缓存
    @PostMapping("/createOrderWithCacheV2/{sid}")
    public String createOrderWithCacheV2(@PathVariable int sid);



}
