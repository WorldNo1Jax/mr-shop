package com.baidu.shop.business.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.OrderService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.OrderDetailEntity;
import com.baidu.shop.entity.OrderEntity;
import com.baidu.shop.entity.OrderStatusEntity;
import com.baidu.shop.entity.QueryEntity;
import com.baidu.shop.fegin.StockFegin;
import com.baidu.shop.mapper.OrderDetailMapper;
import com.baidu.shop.mapper.OrderMapper;
import com.baidu.shop.mapper.OrderStatusMapper;
import com.baidu.shop.mapper.SkuMapper;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.IdWorker;
import com.baidu.shop.utils.JwtUtils;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @ClassName OrderServiceImpl
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/21
 * @Version V1.0
 **/
@RestController
public class OrderServiceImpl extends BaseApiService implements OrderService {

    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderStatusMapper orderStatusMapper;
    @Resource
    private StockFegin stockFegin;
    @Resource
    private SkuMapper skuMapper;

    @Resource
    private JwtConfig jwtConfig;
    @Resource
    private IdWorker idWorker;
    @Resource
    private RedisRepository redisRepository;

    private static String GOODS_CAR_PRE = "goods_car_pre";

    @Override
    public Result<JSONObject> getSpuIdByskuId(Long skuId) {

        Integer spuId= skuMapper.getSpuIdByskuId(skuId);

        return this.setResultSuccess(spuId);
    }

    //根据用户Id去查询订单信息
    @Override
    public Result<List<OrderInfo>> getOrderInfoByUserId(QueryEntity queryEntity) {


        //获取订单数据集合
        List<OrderInfo> orderBYstauts = null;
        //总条数
        Integer total = null;

        if(queryEntity.getStatus()==1){
            // userId stauts  查询订单  分页
            List<OrderEntity> orderEntities = orderMapper.selectWeifukuan(
                    queryEntity.getUserId(),queryEntity.getStatus());
            //stauts = 1 总条数
            total = orderMapper.countStauts(queryEntity.getStatus());

            //获取订单数据集合
            orderBYstauts = getOrderBYstauts(orderEntities);

        }else{
            //总条数
            total = orderMapper.count();
            //分页
            PageHelper.startPage(queryEntity.getPage(),queryEntity.getRows());
            Example example = new Example(OrderEntity.class);
            example.createCriteria().andEqualTo("userId",queryEntity.getUserId());


            //通过userId  stauts 去查询订单
            List<OrderEntity> orderEntities = orderMapper.selectByExample(example);

            //获取订单数据集合
            orderBYstauts = getOrderBYstauts(orderEntities);
        }


        PageInfo<OrderInfo> orderInfoPageInfo = new PageInfo<>(orderBYstauts);
                    //        code         总条数         data
        return this.setResult(HTTPStatus.OK,total+"",orderInfoPageInfo);
    }

    //根据不同的status查询的订单   获得订单数据OrderInfo的List集合
    private List<OrderInfo> getOrderBYstauts(List<OrderEntity> orderEntities ){

        //定义 List<OrderInfo>
        List<OrderInfo> orderInfoList = new ArrayList<OrderInfo>();

        //遍历所有的订单
        orderEntities.forEach(order -> {
            OrderEntity orderEntity = orderMapper.selectByPrimaryKey(order.getOrderId());

            //orderInfo 包含订单全部信息
            OrderInfo orderInfo = BaiduBeanUtil.copyProperties(order, OrderInfo.class);

            //在orderInfo 中添加 String类型 orderId  防止精度丢失
            orderInfo.setOderStringId(orderEntity.getOrderId()+"");

            //通过orderId  获得detail信息  自定义sql查询
            List<OrderDetailEntity> orderDetailEntityList = orderDetailMapper.selectDetailByOrderId(order.getOrderId());
            OrderStatusEntity orderStatusEntity = orderStatusMapper.selectByPrimaryKey(orderInfo.getOrderId());

            orderInfo.setOrderDetailList(orderDetailEntityList);
            orderInfo.setOrderStatusEntity(orderStatusEntity);

            orderInfoList.add(orderInfo);
        });
        return orderInfoList;
    }



    // 根据订单Id去查询订单信息
    @Override
    public Result<OrderInfo> getOrderInfoByOrderId(Long orderId) {

        OrderEntity orderEntity = orderMapper.selectByPrimaryKey(orderId);

        //orderInfo 包含订单全部信息
        OrderInfo orderInfo = BaiduBeanUtil.copyProperties(orderEntity, OrderInfo.class);

        Example example = new Example(OrderEntity.class);
        example.createCriteria().andEqualTo("orderId",orderId);

        //通过orderId  获得detail信息
        List<OrderDetailEntity> orderDetailEntityList = orderDetailMapper.selectByExample(example);
        OrderStatusEntity orderStatusEntity = orderStatusMapper.selectByPrimaryKey(orderInfo.getOrderId());

        orderInfo.setOrderDetailList(orderDetailEntityList);
        orderInfo.setOrderStatusEntity(orderStatusEntity);

        return this.setResultSuccess(orderInfo);
    }

    @Transactional
    @Override
    public Result<String> createOrder(OrderDTO orderDTO,String token) {//如果返回Long 存在精度丢失的问题

        long orderId = idWorker.nextId();
        try {
            //通过公钥获取用户信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            OrderEntity orderEntity = new OrderEntity();
            Date date = new Date();
            //生成订单
            orderEntity.setOrderId(orderId);
            orderEntity.setUserId(userInfo.getId()+"");

            orderEntity.setBuyerMessage("1909很强");  //买家留言
            orderEntity.setBuyerNick(userInfo.getUsername()); //昵称
            orderEntity.setBuyerRate(1);//用户是否评论 1:没有
            orderEntity.setSourceType(1);//写死的PC端,如果项目健全了以后,这个值应该是常量
            orderEntity.setInvoiceType(1);//发票类型同上
            orderEntity.setPaymentType(orderDTO.getPayType()); //支付类型
            orderEntity.setCreateTime(date);

            //detail
            List<Long> longs = Arrays.asList(0L);
            List<OrderDetailEntity> orderDetailEntityList = Arrays.asList(orderDTO.getSkuIds().split(",")).stream().map(skuStr -> {

                //通过skuid 去redis查找sku数据
                Car car = redisRepository.getHash(GOODS_CAR_PRE + userInfo.getId(), skuStr, Car.class);
                if (null == car) throw new RuntimeException("数据异常");

                OrderDetailEntity orderDetailEntity = new OrderDetailEntity();

                orderDetailEntity.setSkuId(Long.valueOf(car.getSkuId()));
                orderDetailEntity.setImage(car.getImage());
                orderDetailEntity.setNum(car.getNum());
                orderDetailEntity.setOrderId(orderId);
                orderDetailEntity.setOwnSpec(car.getOwnSpec());
                orderDetailEntity.setPrice(car.getPrice());
                orderDetailEntity.setTitle(car.getTitle());

                longs.set(0, (car.getPrice() * car.getNum()) + longs.get(0));
                return orderDetailEntity;
            }).collect(Collectors.toList());

            orderEntity.setActualPay(longs.get(0)); //设置总金额
            orderEntity.setTotalPay(longs.get(0));

            //订单状态 status
            OrderStatusEntity orderStatusEntity = new OrderStatusEntity();
            orderStatusEntity.setCreateTime(date);
            orderStatusEntity.setOrderId(orderId);
            orderStatusEntity.setStatus(1); //已经创建好订单，但是还没支付
            //入库
            orderMapper.insertSelective(orderEntity);
            orderDetailMapper.insertList(orderDetailEntityList);
            orderStatusMapper.insertSelective(orderStatusEntity);


            //mysql和redis双写一致性问题?????
            orderDetailEntityList.stream().forEach(detail ->{

                DetailDTO detailDTO = new DetailDTO();
                detailDTO.setSkuId(detail.getSkuId());
                detailDTO.setNum(detail.getNum());
                // 更新库存
                stockFegin.getStockByskuIdAndNum(detailDTO);

            });
            Arrays.asList(orderDTO.getSkuIds().split(",")).stream().forEach(sukidStr->{
                //通过用户id和skuid删除购物车中的数据
                redisRepository.delHash(GOODS_CAR_PRE+userInfo.getId(),sukidStr);


            });


        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResult(HTTPStatus.OK,"",orderId+"");
    }
}
