package com.baidu.shop.service.impl;

import com.baidu.shop.entity.OrderEntity;
import com.baidu.shop.entity.StockEntity;
import com.baidu.shop.mapper.OrderMapper;
import com.baidu.shop.mapper.StockMapper;
import com.baidu.shop.service.MiaoShaService;
import com.baidu.shop.utils.IdWorker;
import com.baidu.shop.utils.KeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName MiaoShaServiceImpl
 * @Description: TODO
 * @Author jlz
 * @Date 2020-10-29 20:11
 * @Version V1.0
 **/
@Service
@Slf4j
public class MiaoShaServiceImpl implements MiaoShaService {

    @Resource
    private StockMapper stockMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private IdWorker idWorker;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void delStockCountCache(Long id) {
        String hashKey = KeyConstants.STOCK_COUNT + "_" + id;
        stringRedisTemplate.delete(hashKey);
        log.info("删除商品id：[{}] 缓存", id);
    }

    // 创建正确订单：验证库存 + 下单 乐观锁 + 更新订单信息到缓存
    @Transactional
    @Override
    public void createOrderByMq(String sid, Integer userId) {
        StockEntity stock;
        //校验库存（不要学我在trycatch中做逻辑处理，这样是不优雅的。这里这样处理是为了兼容之前的秒杀系统文章）
        try {
            stock = checkStock(Long.valueOf(sid));
        } catch (Exception e) {
            log.info("库存不足！");
            return;
        }
        //乐观锁 + 版本号 更新库存
        log.info("查询数据库，尝试更新库存");
        //int count = stockMapper.updateStockByOptimistic(stock.getId(),stock.getSale());
        int count = stockMapper.updateStockByOptimistic(stock.getId(),stock.getVersion());
        if (!(count != 0)) {
            log.warn("扣减库存失败，库存已经为0");
            return;
        }

        log.info("扣减库存成功，剩余库存：[{}]", stock.getCount() - stock.getSale() - 1);
        delStockCountCache(Long.valueOf(sid));
        log.info("删除库存缓存");

        //创建订单
        log.info("写入订单至数据库");
        OrderEntity order = new OrderEntity();
        long orderId = idWorker.nextId();
        order.setId(orderId);
        order.setStockId(stock.getId());
        order.setUserId(userId);
        orderMapper.insertSelective(order);

        log.info("写入订单至缓存供查询");
        createOrderWithUserInfoInCache(stock, userId);
        log.info("下单完成");
    }

    // 检验用户是否抢购过商品
    @Override
    public Boolean checkUserOrderInfoInCache(Long sid, Integer userId) {
        String key = KeyConstants.USER_HAS_ORDER + "_" + sid;
        log.info("检查用户Id：[{}] 是否抢购过商品Id：[{}] 检查Key：[{}]", userId, sid, key);
        return stringRedisTemplate.opsForSet().isMember(key, userId.toString());
    }

    //检查缓存中库存  --》 缓存中没有 --》查询数据库 写入缓存
    @Override
    public Integer getStockCount(Long sid) {
        Integer stockLeft;
        stockLeft = getStockCountByCache(sid);
        log.info("缓存中取得库存数：[{}]", stockLeft);
        if (stockLeft == null) {
            stockLeft = getStockCountByDB(sid);
            log.info("缓存未命中，查询数据库，并写入缓存");
            setStockCountCache(sid, stockLeft);
        }
        return stockLeft;
    }


    /**
     * 创建订单：保存用户订单信息到缓存
     * @param stock
     * @return 返回添加的个数
     */
    private Long createOrderWithUserInfoInCache(StockEntity stock, Integer userId) {
        String key = KeyConstants.USER_HAS_ORDER+ "_" + stock.getId().toString();
        log.info("写入用户订单数据Set：[{}] [{}]", key, userId.toString());
        return stringRedisTemplate.opsForSet().add(key, userId.toString());
    }


    //校验库存
    private StockEntity checkStock(Long sid) {
        StockEntity StockEntity = stockMapper.selectByPrimaryKey(sid);
        // 当已售数量 == 库存  说明库存不足 秒杀结束
        if (StockEntity.getSale().equals(StockEntity.getCount())) {
            throw new RuntimeException("库存不足");
        }
        return StockEntity;
    }


    private Integer getStockCountByCache(Long id) {
        String hashKey = KeyConstants.STOCK_COUNT + "_" + id;
        String countStr = stringRedisTemplate.opsForValue().get(hashKey);
        if (countStr != null) {
            return Integer.parseInt(countStr);
        } else {
            return null;
        }
    }

    private int getStockCountByDB(Long id) {
        StockEntity stock = stockMapper.selectByPrimaryKey(id);
        return stock.getCount() - stock.getSale();
    }

    private void setStockCountCache(Long id, int count) {
        String hashKey = KeyConstants.STOCK_COUNT + "_" + id;
        String countStr = String.valueOf(count);
        log.info("写入商品库存缓存: [{}] [{}]", hashKey, countStr);
        stringRedisTemplate.opsForValue().set(hashKey, countStr, 3600, TimeUnit.SECONDS);
    }
}
