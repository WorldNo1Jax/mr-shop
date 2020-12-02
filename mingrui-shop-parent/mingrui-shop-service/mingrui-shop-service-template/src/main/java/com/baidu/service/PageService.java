package com.baidu.service;

import org.springframework.stereotype.Service;

import java.util.Map;


public interface PageService    {

    Map<String, Object> getPageInfoBySpuId(Integer spuId);
}
