package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.component.MrRabbitMQ;
import com.baidu.shop.constant.MqMessageConstant;
import com.baidu.shop.dto.DetailDTO;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SkuEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.entity.StockEntity;


import com.baidu.shop.feign.SearchFeign;
import com.baidu.shop.feign.TemplateFeg;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.json.JSONObject;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsServiceImpl
 * @Description: TODO
 * @Author ljc
 * @Date 2020/9/7
 * @Version V1.0
 **/
@RestController()
public class GoodsServiceImpl<TemplateFegin1> extends BaseApiService implements GoodsService {

    @Resource
    private SpuMapper spuMapper;
    @Resource
    private SpuDetailMapper spuDetailMapper;
    @Resource
    private SkuMapper skuMapper;
    @Resource
    private StockMapper stockMapper;

    @Resource
    private TemplateFeg templateFegin;
    @Resource
    private SearchFeign searchFeign;

    @Resource
    private MrRabbitMQ mrRabbitMQ;

    //通过skuId 和订单商品数量去更新 库存
    @Override
    public Result<JSONObject> getStockByskuIdAndNum(DetailDTO detailDTO) {

        stockMapper.updateByskuIdAndNum(detailDTO.getSkuId(),detailDTO.getNum());
        return this.setResultSuccess();
    }

    //通过skuId去查询sku数据
    @Override
    public Result<SkuEntity> getSkuById(Long skuId) {

        SkuEntity skuEntity = skuMapper.selectByPrimaryKey(skuId);

        return this.setResultSuccess(skuEntity);
    }

    //上架 和下架
    @Transactional()
    @Override
    public Result<JsonObject> soldAdd(SpuDTO spuDTO) {

        if(ObjectUtil.isNull(spuDTO.getSaleable())
                && spuDTO.getSaleable()!=1 && spuDTO.getSaleable()!=0) return this.setResultError("saleable 只能是1 或0");

        SpuEntity spuEntity = new SpuEntity();
        spuEntity.setId(spuDTO.getId());
        spuEntity.setSaleable(spuDTO.getSaleable());
        spuMapper.updateByPrimaryKeySelective(spuEntity);

        return this.setResultSuccess();
    }

    //删除 商品
    @Transactional
    @Override
    public Result<JsonObject> deleteGoods(Integer spuId) {
        //删除spu
        //删除spudetail
        spuMapper.deleteByPrimaryKey(spuId);
        spuDetailMapper.deleteByPrimaryKey(spuId);

        //删除sku
        //删除stock
        this.deleteSkuAndStock(spuId);

        //事务完成之后 需要执行的方法
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //RabbitMQ 发送消息 删除静态HTML文件
                mrRabbitMQ.send(spuId+"", MqMessageConstant.SPU_ROUT_KEY_DELETE);
            };
        });

        return this.setResultSuccess();
    }

    //修改商品
    @Transactional
    @Override
    public Result<JsonObject> editInfo(SpuDTO spuDTO) {

        Date date = new Date();
        //修改spu
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        spuMapper.updateByPrimaryKeySelective(spuEntity);
        //修改spuDetail
        spuDetailMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(),SpuDetailEntity.class));

        this.deleteSkuAndStock(spuDTO.getId());
        this.addSkuStock(spuDTO,spuEntity.getId(),date);

        //事务完成之后 需要执行的方法
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //RabbitMQ 发送消息
                mrRabbitMQ.send(spuDTO.getId()+"", MqMessageConstant.SPU_ROUT_KEY_UPDATE);
            };
        });

        return this.setResultSuccess();
    }


    @Transactional()
    @Override
    public Result<List<SkuDTO>> getSkuBySpuId(Integer spuId) {

        List<SkuDTO> list =  skuMapper.selectSkuAndStockBYspuId(spuId);
        return this.setResultSuccess(list);
    }

    @Transactional()
    @Override
    public Result<SpuDetailEntity> getDetailBySpuId(Integer spuId) {

        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuId);
        return this.setResultSuccess(spuDetailEntity);
    }

    //查询spu信息
    @Transactional()
    @Override
    public Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO) {

        //分页
        if(ObjectUtil.isNotNull(spuDTO.getPage()) && ObjectUtil.isNotNull(spuDTO.getRows()))
            PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());

        List<SpuDTO> list = spuMapper.list(spuDTO);
        Integer total = spuMapper.count(spuDTO);

       // return this.setResult(HTTPStatus.OK,total+"",list);
        return this.setResultSuccess(list);
//        //分页
//        if(ObjectUtil.isNotNull(spuDTO.getPage()) && ObjectUtil.isNotNull(spuDTO.getRows()))
          //PageHelper.startPage(spuDTO.getPage(),spuDTO.getRows());

//        //构建条件查询
//        Example example = new Example(SpuEntity.class);
//        Example.Criteria criteria = example.createCriteria();
//
//        if(spuDTO!=null){
//            if(StringUtil.isNotEmpty(spuDTO.getTitle()))  criteria.andLike("title","%" + spuDTO.getTitle() + "%");
//            if(ObjectUtil.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() != 2)  criteria.andEqualTo("saleable",spuDTO.getSaleable());
//            if(ObjectUtil.isNotNull(spuDTO.getSort())) example.setOrderByClause(spuDTO.getOrderByClause());
//        }
//
//        List<SpuEntity> list = spuMapper.selectByExample(example);
//        List<SpuDTO> spuDTOList = spuMapper.getByIddata(spuDTO.getPage()-1,spuDTO.getRows());
//
//        PageInfo<SpuEntity> pageInfo = new PageInfo<>(list);
//        //spuDTOList的pageInfo 中没有总条数  从list的PageInfo中取值
//        return this.setResult(HTTPStatus.OK,pageInfo.getTotal()+"",spuDTOList);
    }

    //新增商品
    @Transactional
    @Override
    public Result<JsonObject> saveGoods(SpuDTO spuDTO) {
        //新增spu
        SpuEntity spuEntity = BaiduBeanUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setSaleable(1);
        spuEntity.setValid(1);
        final Date date = new Date();//保持两个时间一致
        spuEntity.setCreateTime(date);
        spuEntity.setLastUpdateTime(date);
        spuMapper.insertSelective(spuEntity);

        //新增spuDetail
        SpuDetailEntity spuDetailEntity = BaiduBeanUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailEntity.setSpuId(spuEntity.getId());
        spuDetailMapper.insertSelective(spuDetailEntity);

        this.addSkuStock(spuDTO,spuEntity.getId(),date);

        //事务完成之后 需要执行的方法
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                //RabbitMQ 发送消息
                mrRabbitMQ.send(spuEntity.getId()+"", MqMessageConstant.SPU_ROUT_KEY_SAVE);
            };
        });

        return this.setResultSuccess();

    }



    //删除 sku stock
    private void deleteSkuAndStock(Integer id){
        Example example = new Example(SkuEntity.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",id);
        List<SkuEntity> skuEntities = skuMapper.selectByExample(example);
        List<Long> skuIdList = skuEntities.stream().map(sku -> sku.getId()).collect(Collectors.toList());
        if (skuIdList.size()>0) {
            skuMapper.deleteByIdList(skuIdList);
            stockMapper.deleteByIdList(skuIdList);
        }
    }

    //批量新增 sku stock
    private void addSkuStock(SpuDTO spuDTO,Integer id,Date date){
        spuDTO.getSkus().stream().forEach(skuDto -> {
            SkuEntity skuEntity = BaiduBeanUtil.copyProperties(skuDto, SkuEntity.class);
            skuEntity.setSpuId(id);
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);

            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDto.getStock());
            stockMapper.insertSelective(stockEntity);
        });
    }
}
