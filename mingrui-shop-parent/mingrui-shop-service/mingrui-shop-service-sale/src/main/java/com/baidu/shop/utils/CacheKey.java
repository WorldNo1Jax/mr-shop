package com.baidu.shop.utils;

/**
 * @ClassName CacheKey
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/28
 * @Version V1.0
 **/
public enum CacheKey {

    HASH_KEY("miaosha_v1_user_hash"),
    LIMIT_KEY("miaosha_v1_user_limit"),
    STOCK_COUNT("miaosha_v1_stock_count"),
    USER_HAS_ORDER("miaosha_v1_user_has_order");

    private String key;
    private CacheKey(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }
}