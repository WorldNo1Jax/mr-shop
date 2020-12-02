package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONArray;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.comment.MyLog;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.dto.Car;
import com.baidu.shop.dto.CarSkuIdDTO;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.SkuEntity;
import com.baidu.shop.fegin.GoodsFeign;
import com.baidu.shop.fegin.Specificationfenign;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.service.CarService;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

/**
 * @ClassName CarServiceImpl
 * @Description: TODO
 * @Author ljc
 * @Date 2020/10/19
 * @Version V1.0
 **/
@RestController(value = "car")
@Slf4j
public class CarServiceImpl extends BaseApiService implements CarService {

    @Resource
    private RedisRepository redisRepository;
    @Autowired
    private JwtConfig jwtConfig;
    @Autowired
    private Specificationfenign specificationfenign;
    @Autowired
    private GoodsFeign goodsFeign;

    private static String GOODS_CAR_PRE = "goods_car_pre";

    //删除购物车中商品 多删
    @Override
    public Result<JSONObject> delCarAll(CarSkuIdDTO carSkuIdDTO, String token) {

        try {
            //通过公钥获取用户信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            //将JSON字符串转换成List
            Arrays.asList(carSkuIdDTO.getSkuIds().split(",")).forEach(skuId->{
                //根据 用户Id skuId删除购物车商品
                redisRepository.delHash(GOODS_CAR_PRE+userInfo.getId(),skuId+"");
            });

            return setResultSuccess();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResultError("删除失败");

    }

    //删除购物车中商品
    @Override
    public Result<JSONObject> delCar(Long skuId, String token) {

        try {
            //通过公钥获取用户信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            //根据 用户Id skuId删除购物车商品
            redisRepository.delHash(GOODS_CAR_PRE+userInfo.getId(),skuId+"");


            return setResultSuccess();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResultError("删除失败");
    }

    //修改商品在购物车中的数量
    @Override
    public Result<JSONObject> carNumUpdate(Long skuId, Integer type, String token) {

        try {
            //通过公钥获取用户信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());
            Car car = redisRepository.getHash(GOODS_CAR_PRE + userInfo.getId(), skuId + "", Car.class);

            car.setNum(type==1?car.getNum()+1:car.getNum()-1);

            redisRepository.setHash(GOODS_CAR_PRE+userInfo.getId(),skuId+"",JSONUtil.toJsonString(car));

            return setResultSuccess();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return setResultError("error");
    }

    //查询购物车
    @Override
    public Result<JSONObject> getUserGoodsCar(String token) {

        List<Car> carList = new ArrayList<>();

        try {
            //通过公钥获取用户信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            //通过用户Id 在redis中获得数据
            Map<String, String> map = redisRepository.getHash(GOODS_CAR_PRE + userInfo.getId());

            map.forEach((k,v)->{
                carList.add(JSONUtil.toBean(v,Car.class));
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.setResultSuccess(carList);
    }

    //合并购物车
    @Override
    public Result<JSONObject> mergeCar(String clientCarList, String token) {

        //将JSON字符串转换成JSON对象
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(clientCarList);
        //将json对象属性为clientCarList的数据取出来,并且转换成List集合
        List<Car> carList = com.alibaba.fastjson.JSONObject.parseArray
                        (jsonObject.getJSONArray("clientCarList").toJSONString(), Car.class);

        //遍历增加到购物车
        carList.stream().forEach(car ->this.addCar(car,token));
        return setResultSuccess();
    }

    //添加商品到购物车
    @Override
    @MyLog(operationModel = "购物车模块",operationType = "POST",operation = "添加商品到购物车")
    public Result<JSONObject> addCar(Car car,String token) {

        try {
            //通过公钥从token中获取用户信息
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            //通过用户 Id和skuId获取redis中购物车中的数据
            Car userCarItem = redisRepository.getHash(GOODS_CAR_PRE+userInfo.getId(), car.getSkuId()+"", Car.class);
            log.debug("从redis中获取数据 : " + userCarItem);

            if(userCarItem==null){
                //如果redis中没有购物车数据 -->通过skuId查找sku信息 -->添加到redis中
                log.debug("当前用户{} 没有将sku: {} 添加到购物车",userInfo.getUsername(),car.getSkuId());
                Result<SkuEntity> skuResult = goodsFeign.getSkuById(car.getSkuId());
                if(skuResult.getCode()==200){
                    SkuEntity sku = skuResult.getData();

                    car.setUserId(userInfo.getId());
                    car.setPrice(sku.getPrice().longValue());
                    car.setTitle(sku.getTitle());

                    //判断images的值是否为空,如果为空的话就返回空,如果不为空的话通过,分隔取第一张图片即可
                    car.setImage(StringUtil.isEmpty(sku.getImages())? "":sku.getImages().split(",")[0]);

                    Map<String, Object> map = JSONUtil.toMap(sku.getOwnSpec());
                    HashMap<String, Object> newMap = new HashMap<>();

                    map.forEach((key,v)->{
                        newMap.put(specificationfenign.getOwnSpecName(key),v);
                    });
                    car.setOwnSpec(JSONUtil.toJsonString(newMap));
                    //car.setOwnSpec(sku.getOwnSpec());
                }
                //添加sku数据到购物车
                boolean b = redisRepository.setHash(GOODS_CAR_PRE + userInfo.getId(), car.getSkuId() + "",
                        JSON.toJSONString(car));

                log.debug("添加到redis结果 : {} , hashkey : {} , mapkey : {} ",b,GOODS_CAR_PRE + userInfo.getId(),car.getSkuId());
            }else{
                log.debug("当前用户 {} 以前添加过sku : {} 的数据,更改购物车对应的商品数量为 : {}",
                        userInfo.getUsername(),car.getSkuId(),userCarItem.getNum() + car.getNum());
                //数量合并
                userCarItem.setNum(userCarItem.getNum()+car.getNum());

                boolean b = redisRepository.setHash(GOODS_CAR_PRE + userInfo.getId(),
                        car.getSkuId() + "", JSON.toJSONString(userCarItem));
                log.debug("添加到redis结果 : {} , hashkey : {} , mapkey : {} ,数量 :{}",
                        b,GOODS_CAR_PRE + userInfo.getId(),car.getSkuId(),userCarItem.getNum());

            }

        }catch (Exception e){   //进入异常 token有问题
            e.printStackTrace();
        }


        return setResultSuccess();
    }



}
