package com.baidu.shop.web;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.rabbitMq.RabbitMQ;

import com.baidu.shop.service.MiaoShaService;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.MqConstants;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @ClassName MiaoShaContorller
 * @Description: TODO
 * @Author jlz
 * @Date 2020-10-29 20:10
 * @Version V1.0
 **/
@RestController
@Slf4j
public class MiaoShaContorller extends BaseApiService {

    @Autowired
    private MiaoShaService service;

    @Autowired
    private RabbitMQ rabbitMQ;

    @Resource
    private JwtConfig jwtConfig;

    // Guava令牌桶：每秒放行10个请求
    RateLimiter rateLimiter = RateLimiter.create(10);

    /**8.0
     * 下单接口：异步处理订单
     * @param sid
     * @return
     */
    @RequestMapping(value = "miaosha/createUserOrderWithMq", method = {RequestMethod.GET})
    public Result<JSONObject> createUserOrderWithMq(@RequestParam(value = "sid") String sid,
                                        @CookieValue("MRSHOP_TOKEN") String token) {
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());
            // 检查缓存中该用户是否已经下单过
            Boolean hasOrder = service.checkUserOrderInfoInCache(Long.valueOf(sid), userInfo.getId());
            if (hasOrder != null && hasOrder) {
                log.info("该用户已经抢购过");
                return this.setResult(200,"","你已经抢购成功，请耐心等待");
            }
            // 没有下单过，检查缓存中商品是否还有库存
            log.info("没有抢购过，检查缓存中商品是否还有库存");
            Integer count = service.getStockCount(Long.valueOf(sid));
            if (count == 0) {
                return this.setResultError("秒杀请求失败，库存不足..");
            }

            // 有库存，则将用户id和商品id封装为消息体传给消息队列处理
            // 注意这里的有库存和已经下单都是缓存中的结论，存在不可靠性，在消息队列中会查表再次验证
            log.info("有库存：[{}]", count);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sid", sid);
            jsonObject.put("userId", userInfo.getId());
            rabbitMQ.send(jsonObject.toJSONString(), MqConstants.MIAOSHA_ROUT_KEY_ADDORDER);
            return this.setResult(200,"","秒杀请求提交成功");
        } catch (Exception e) {
            log.error("下单接口：异步处理订单异常：", e);
            return this.setResultError("秒杀请求失败，服务器正忙...");
        }
    }
}
