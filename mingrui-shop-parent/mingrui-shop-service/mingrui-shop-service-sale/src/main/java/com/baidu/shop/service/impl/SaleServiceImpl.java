package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.entity.Stock;
import com.baidu.shop.entity.StockOrder;

import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.StockMapper;
import com.baidu.shop.mapper.StockOrderMapper;
import com.baidu.shop.mapper.UserMapper;
import com.baidu.shop.redis.RedisRepositroy;
import com.baidu.shop.service.SaleService;
import com.baidu.shop.utils.CacheKey;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName SaleController
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/27
 * @Version V1.0
 **/

@RestController
@Slf4j
public class SaleServiceImpl extends BaseApiService  implements SaleService{


    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private StockOrderMapper stockOrderMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisRepositroy redisRepositroy;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String SALT = "randomString";

    //允许单个用户访问10次
    private static final Integer ALLOW_COUNT = 10;



    //每秒只发出10个令牌
    RateLimiter rateLimiter = RateLimiter.create(10);





    //=============================================

    //   下单接口：先更新数据库，再删缓存
    @Override
    public String createOrderWithCacheV2(int sid) {
        int count = 0;
        try {
            // 完成扣库存下单 事务
            createPessimisticOrder(sid);
            // 删除库存缓存
            delStockCountCache(sid);
            // 再次去缓存中查 -->没有数据 -->数据库查 -->写到缓存中
            count = getStockByCache(sid);
        } catch (Exception e) {
            log.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        log.info("购买成功，剩余库存为: [{}]", count);
        return String.format("购买成功，剩余库存为：%d", count);
    }

    // 删除缓存
    private void delStockCountCache(int id) {
        String hashKey = CacheKey.STOCK_COUNT.getKey() + "_" + id;
        stringRedisTemplate.delete(hashKey);
        log.info("删除商品id：[{}] 缓存", id);
    }


    //------------------------------------------------------
    /**
     * 查询库存：通过数据库查询库存
     */
    @Override
    public String getStockByDB(int sid) {

        int count;
        try {
            count = getStockCountByDB(sid);
        } catch (Exception e) {
            log.error("查询库存失败：[{}]", e.getMessage());
            return "查询库存失败";
        }
        log.info("商品Id: [{}] 剩余库存为: [{}]", sid, count);
        return String.format("商品Id: %d 剩余库存为：%d", sid, count);

    }

    private int getStockCountByDB(int sid) {

        Stock stock = checkStock(sid);

        return  stock.getCount() - stock.getSale();
    }

    /**
     * 查询库存：通过缓存查询库存
     * 缓存命中：返回库存
     * 缓存未命中：查询数据库写入缓存并返回
     */
    @Override
    public Integer getStockByCache(int sid) {

        Integer count;
        try {
            count = getStockCountByCache(sid);
            if (count == null) {
                count = getStockCountByDB(sid);
                log.info("缓存未命中，查询数据库，并写入缓存");
                setStockCountToCache(sid, count);

                return count;
            }
        } catch (Exception e) {
            log.error("查询库存失败：[{}]", e.getMessage());
            return null;
        }
        log.info("商品Id: [{}] 剩余库存为: [{}]", sid, count);
        return count;
    }

    // 新增到redis缓存
    private void setStockCountToCache(int sid, Integer count) {

        stringRedisTemplate.opsForValue().set(sid+"",count+"",3600,TimeUnit.SECONDS);

    }

    //去redis中查询数据
    private Integer getStockCountByCache(Integer sid) {

        try {
            String s = stringRedisTemplate.opsForValue().get(sid);
            return Integer.parseInt(s);
        }catch (Exception e){
            log.info(e.getMessage());
            return null;
        }

    }

    //需要验证的抢购接口 + 单用户限制访问频率
    @Override
    public String createOrderWithVerifiedUrlAndLimit(Integer sid, Integer userId, String verifyHash) {

        int stockLeft;  //库存剩余
        try {
            //用戶 的访问次数
            int count = addUserCount(userId);
            log.info("用户截至该次的访问次数为: [{}]", count);
            boolean isBanned = getUserIsBanned(userId);
            if (isBanned) {
                return "购买失败，超过频率限制";
            }

            // 验证是否在抢购时间内
            log.info("请自行验证是否在抢购时间内,假设此处验证成功");

            // 验证hash值合法性
            String hashKey = CacheKey.HASH_KEY + "_" + sid + "_" + userId;
            String verifyHashInRedis = stringRedisTemplate.opsForValue().get(hashKey);
            if (!verifyHash.equals(verifyHashInRedis)) {
                throw new Exception("hash值与Redis中不符合");
            }
            log.info("验证hash值合法性成功");

            // 检查用户合法性
            UserEntity user = userMapper.selectByPrimaryKey(userId.longValue());
            if (user == null) {
                throw new Exception("用户不存在");
            }
            log.info("用户信息验证成功：[{}]", user.toString());

            // 检查商品合法性
            Stock stock = checkStock(sid);
            if (stock == null) {
                throw new Exception("商品不存在");
            }
            log.info("商品信息验证成功：[{}]", stock.toString());

            //乐观锁更新库存
            saleStockOptimistic(stock);
            log.info("乐观锁更新库存成功");

            //创建订单
            createOrder(stock);
            log.info("创建订单成功");

            stockLeft = stock.getCount() - (stock.getSale()+1);
            log.info("购买成功，剩余库存为: [{}]", stockLeft);
        } catch (Exception e) {
            log.error("购买失败：[{}]", e.getMessage());
            return e.getMessage();
        }
        return String.format("购买成功，剩余库存为：%d", stockLeft);

    }

    //需要验证的抢购接口
    @Override
    public String createOrderWithVerifiedUrl(Integer sid, Integer userId, String verifyHash) {

        int stockLeft;

        try {
            // 验证是否在抢购时间内
            log.info("请自行验证是否在抢购时间内,假设此处验证成功");

            // 验证hash值合法性
            String hashKey = CacheKey.HASH_KEY + "_" + sid + "_" + userId;
            String verifyHashInRedis = stringRedisTemplate.opsForValue().get(hashKey);
            if (!verifyHash.equals(verifyHashInRedis)) {
                throw new Exception("hash值与Redis中不符合");
            }
            log.info("验证hash值合法性成功");

            // 检查用户合法性
            UserEntity user = userMapper.selectByPrimaryKey(userId.longValue());
            if (user == null) {
                throw new Exception("用户不存在");
            }
            log.info("用户信息验证成功：[{}]", user.toString());

            // 检查商品合法性
            Stock stock = checkStock(sid);
            if (stock == null) {
                throw new Exception("商品不存在");
            }
            log.info("商品信息验证成功：[{}]", stock.toString());

            //乐观锁更新库存
            saleStockOptimistic(stock);
            log.info("乐观锁更新库存成功");

            //创建订单
            createOrder(stock);
            log.info("创建订单成功");

            stockLeft = stock.getCount() - (stock.getSale()+1);
        }catch (Exception e){
            log.error("抢购失败: [{}]",e.getMessage());
            return e.getMessage();
        }

        return String.format("购买成功，剩余库存为：%d", stockLeft);
    }

    //每当访问订单接口，则增加一次访问次数，写入Redis
    public int addUserCount(Integer userId) throws Exception {
        String limitKey = CacheKey.LIMIT_KEY + "_" + userId;
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        log.info("limitKey : "+ limitKey+" limitNum " +limitNum);
        int limit = -1;      //計數
        if (limitNum == null) {
            stringRedisTemplate.opsForValue().set(limitKey, "0", 3600, TimeUnit.SECONDS);
        } else {
            limit = Integer.parseInt(limitNum) + 1;
            stringRedisTemplate.opsForValue().set(limitKey, String.valueOf(limit), 3600, TimeUnit.SECONDS);
        }
        return limit;
    }

    //从Redis读出该用户的访问次数，超过10次则不让购买了！不能让张三做法外狂徒。
    public boolean getUserIsBanned(Integer userId) {
        String limitKey = CacheKey.LIMIT_KEY + "_" + userId;
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        if (limitNum == null) {
            log.error("该用户没有访问申请验证值记录，疑似异常");
            return true;
        }
        return Integer.parseInt(limitNum) > ALLOW_COUNT;
    }


    //获取验证码
    @Override
    public String getVerifyHash(Integer sid, Integer userId) {

        String verifyHash;
        try {
            // 验证是否在抢购时间内
            log.info("请自行验证是否在抢购时间内");


            // 检查用户合法性
            UserEntity user = userMapper.selectByPrimaryKey(userId.longValue());
            if (user == null) {
                throw new Exception("用户不存在");
            }
            log.info("用户信息：[{}]", user.toString());

            // 检查商品合法性
            Stock stock = checkStock(sid);
            if (stock == null) {
                throw new Exception("商品不存在");
            }
            log.info("商品信息：[{}]", stock.toString());

            // 生成hash
            String verify = SALT + sid + userId; //CacheKey.HASH_KEY 盐
            verifyHash = DigestUtils.md5DigestAsHex(verify.getBytes());

            // 将hash和用户商品信息存入redis
            String hashKey = CacheKey.HASH_KEY + "_" + sid + "_" + userId;
            //                                 用户商品信息    验证码    缓存3600秒
            stringRedisTemplate.opsForValue().set(hashKey, verifyHash, 3600, TimeUnit.SECONDS);
            log.info("Redis写入：[{}] [{}]", hashKey, verifyHash);



        } catch (Exception e) {
            log.error("获取验证hash失败，原因：[{}]", e.getMessage());
            return "获取验证hash失败";
        }


        return String.format("请求抢购验证hash值为：%s", verifyHash);
    }

    // 乐观锁更新库存
    @Override
    public String createWrongOrder(@PathVariable int sid) {

        log.info("购买物品编号sid=[{}]", sid);
        int id = 0;
        try {
            // 更新库存  生成订单
            id = this.createOmOrder(sid);

            log.info("创建订单id: [{}]", id);
        } catch (Exception e) {
            log.error("Exception", e);
        }
        //返回 订单id
        return String.valueOf(id);
    }

    /**
     * 乐观锁更新库存 + 令牌桶限流
     * @param sid
     * @return
     */
    @Override
    public String createOptimisticOrder(@PathVariable int sid) {
        /**
         *
         *
         */

        // 阻塞式获取令牌
        //log.info("等待时间" + rateLimiter.acquire());
        // 非阻塞式获取令牌
        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            log.warn("你被限流了，真不幸，直接返回失败");
            return "购买失败，库存不足";
        }
        int id;
        try {
            //更新库存 创建订单
            id = this.createOmOrder(sid);
            log.info("购买成功，剩余库存为: [{}]", id);
        } catch (Exception e) {
            log.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        return String.format("购买成功，剩余库存为：%d", id);
    }

    // 事务for update更新库存
    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public String createPessimisticOrder(@PathVariable int sid) {
        int id;
        try {
            //校验库存  加行锁
            Stock stock = checkStockForUpdate(sid);
            //乐观锁更新库存  解决超卖问题
            saleStockOptimistic(stock);
            //创建订单
            createOrder(stock);

            id = stock.getCount() - (stock.getSale()+1);

            log.info("购买成功，剩余库存为: [{}]", id);
        } catch (Exception e) {
            log.error("购买失败：[{}]", e.getMessage());
            return "购买失败，库存不足";
        }
        return String.format("购买成功，剩余库存为：%d", id);
    }


    //校验库存
    private Stock checkStock(int sid) {
        Stock stock = stockMapper.selectByPrimaryKey(sid);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    //校验库存 ➕ 行锁 for update
    private Stock checkStockForUpdate(int sid) {
        Stock stock = stockMapper.getStockByIdForUpdate(sid);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    private int  createOmOrder(int sid){

        //校验库存
        Stock stock = checkStock(sid);
        //乐观锁更新库存  解决超卖问题
        saleStockOptimistic(stock);
        //创建订单
        int id = createOrder(stock);

        return stock.getCount() - (stock.getSale()+1);

    }
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public int  createOmOrderForUpdate(int sid){

        //校验库存  加行锁
        Stock stock = checkStockForUpdate(sid);
        //乐观锁更新库存  解决超卖问题
        saleStockOptimistic(stock);
        //创建订单
        int id = createOrder(stock);

        return stock.getCount() - (stock.getSale()+1);

    }

    //  ForUpdate
    public int createPessimisticOrderBysid(int sid){
        //校验库存(悲观锁for update)
        Stock stock = checkStock(sid);
        //更新库存
        saleStock(stock);
        //创建订单
        int id = createOrder(stock);
        return stock.getCount() - (stock.getSale());

    }



    // 加版本号 更新内存
    private void saleStockOptimistic(Stock stock) {
        log.info("查询数据库，尝试更新库存");
        int count = stockMapper.updateStockByOptimistic(stock.getId(),stock.getVersion());
        if (count == 0){
            throw new RuntimeException("并发更新库存失败，version不匹配") ;
        }
    }

    // 更新库存
    private int saleStock(Stock stock) {
        //已售 + 1
        stock.setSale(stock.getSale() + 1);
        return stockMapper.updateByPrimaryKeySelective(stock);
    }

    //创建订单
    private int createOrder(Stock stock) {
        StockOrder order = new StockOrder();
        order.setSid(stock.getId());
        order.setName(stock.getName());
        int id = stockOrderMapper.insertSelective(order);
        return id;
    }

}
