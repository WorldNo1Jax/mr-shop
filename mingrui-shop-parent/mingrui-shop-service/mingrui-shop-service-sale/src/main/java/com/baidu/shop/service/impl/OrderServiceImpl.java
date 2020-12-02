package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.entity.Stock;
import com.baidu.shop.entity.StockOrder;
import com.baidu.shop.mapper.StockMapper;
import com.baidu.shop.mapper.StockOrderMapper;
import com.baidu.shop.service.OrderService;
import com.baidu.shop.service.SaleService;
import com.baidu.shop.utils.CacheKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName OrderServiceImpl
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/28
 * @Version V1.0
 **/
@RestController
@Slf4j
public class OrderServiceImpl extends BaseApiService implements OrderService {

    @Resource
    private StockMapper stockMapper;
    @Resource
    private StockOrderMapper stockOrderMapper;


    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private SaleService saleService;

    // 创建正确订单：验证库存 + 下单乐观锁 + 更新订单信息到缓存
    @Override
    public void createOrderByMq(Integer sid, Integer userId) {
        Stock stock;

        try {
            stock = checkStock(sid);
        } catch (Exception e) {
            log.info("库存不足！");
            return;
        }
        //乐观锁更新库存
        boolean updateStock = saleStockOptimistic(stock);
        if (!updateStock) {
            log.warn("扣减库存失败，库存已经为0");
            return;
        }

        log.info("扣减库存成功，剩余库存：[{}]", stock.getCount() - stock.getSale() - 1);
        delStockCountCache(sid);
        log.info("删除库存缓存");

        //创建订单
        log.info("写入订单至数据库");
        createOrderWithUserInfoInDB(stock, userId);
        log.info("写入订单至缓存供查询");
        createOrderWithUserInfoInCache(stock, userId);
        log.info("下单完成");

    }

    /**
     *  异步处理订单
     * @param sid
     * @param userId
     * @return
     */
    @Override
    public String createUserOrderWithMq(Integer sid, Integer userId) {

        try {
            // 检查缓存中该用户是否已经下单过
            Boolean hasOrder = checkUserOrderInfoInCache(sid, userId);
            if (hasOrder != null && hasOrder) {
                log.info("该用户已经抢购过");
                return "你已经抢购过了，不要太贪心.....";
            }
            

            // 没有下单过，检查缓存中商品是否还有库存
            log.info("没有抢购过，检查缓存中商品是否还有库存");
            Integer count = saleService.getStockByCache(sid);
            if (count == 0) {
                return "秒杀请求失败，库存不足.....";
            }

            // 有库存，则将用户id和商品id封装为消息体传给消息队列处理
            // 注意这里的有库存和已经下单都是缓存中的结论，存在不可靠性，在消息队列中会查表再次验证
            log.info("有库存：[{}]", count);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sid", sid);
            jsonObject.put("userId", userId);
            sendToOrderQueue(jsonObject.toJSONString());
            return "秒杀请求提交成功";
        } catch (Exception e) {
            log.error("下单接口：异步处理订单异常：", e);
            return "秒杀请求失败，服务器正忙.....";
        }
    }


    //校验库存
    private Stock checkStock(int sid) {
        Stock stock = stockMapper.selectByPrimaryKey(sid);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    //  乐观锁 + 加版本号 更新内存
    private boolean saleStockOptimistic(Stock stock) {
        log.info("查询数据库，尝试更新库存");
        int count = stockMapper.updateStockByOptimistic(stock.getId(),stock.getVersion());
        return count != 0;
    }

    // 删除缓存
    private void delStockCountCache(int id) {
        String hashKey = CacheKey.STOCK_COUNT.getKey() + "_" + id;
        stringRedisTemplate.delete(hashKey);
        log.info("删除商品id：[{}] 缓存", id);
    }


    //创建订单：保存用户订单信息到数据库
    private int createOrderWithUserInfoInDB(Stock stock, Integer userId) {
        StockOrder order = new StockOrder();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        order.setUserId(userId);
        return stockOrderMapper.insertSelective(order);
    }


    //创建订单：保存用户订单信息到缓存
    private Long createOrderWithUserInfoInCache(Stock stock, Integer userId) {
        String key = CacheKey.USER_HAS_ORDER.getKey() + "_" + stock.getId().toString();
        log.info("写入用户订单数据Set：[{}] [{}]", key, userId.toString());
        return stringRedisTemplate.opsForSet().add(key, userId.toString());
    }

    // 检查缓存中该用户是否已经下单过
    public Boolean checkUserOrderInfoInCache(Integer sid, Integer userId) throws Exception {
        String key = CacheKey.USER_HAS_ORDER.getKey() + "_" + sid;
        log.info("检查用户Id：[{}] 是否抢购过商品Id：[{}] 检查Key：[{}]", userId, sid, key);
        return stringRedisTemplate.opsForSet().isMember(key, userId.toString());
    }



    //向消息队列delCache发送消息
    private void sendToDelCache(String message) {
        log.info("这就去通知消息队列开始重试删除缓存：[{}]", message);
        this.rabbitTemplate.convertAndSend("delCache", message);
    }

    //向消息队列orderQueue发送消息
    private void sendToOrderQueue(String message) {
        log.info("这就去通知消息队列开始下单：[{}]", message);
        this.rabbitTemplate.convertAndSend("orderQueue", message);
    }

}
