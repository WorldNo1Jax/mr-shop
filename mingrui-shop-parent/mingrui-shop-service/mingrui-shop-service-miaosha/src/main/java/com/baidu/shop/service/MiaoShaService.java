package com.baidu.shop.service;

/**
 * @ClassName MiaoShaService
 * @Description: TODO
 * @Author jlz
 * @Date 2020-10-29 20:11
 * @Version V1.0
 **/
public interface MiaoShaService {
    Boolean checkUserOrderInfoInCache(Long sid, Integer userId);

    Integer getStockCount(Long sid);

    void delStockCountCache(Long parseInt);

    void createOrderByMq(String sid, Integer userId);
}
