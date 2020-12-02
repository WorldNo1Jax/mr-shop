package com.baidu.service.impl;

import com.baidu.feign.BrandFeign;
import com.baidu.feign.CateGoryFeign;
import com.baidu.feign.GoodsFeign;
import com.baidu.feign.Specificationfenign;
import com.baidu.service.PageService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName PageServiceImpl
 * @Description: TODO
 * @Author ljc
 * @Date 2020/9/23
 * @Version V1.0
 **/
@Service
public class PageServiceImpl implements PageService{
    @Resource
    private GoodsFeign goodsFeign;
    @Resource
    private BrandFeign brandFeign;
    @Resource
    private CateGoryFeign cateGoryFeign;
    @Resource
    private Specificationfenign specificationfenign;


    @Override
    public Map<String, Object> getPageInfoBySpuId(Integer spuId) {
        HashMap<String, Object> map = new HashMap<>();

        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        Result<List<SpuDTO>> spuResult = goodsFeign.getSpuInfo(spuDTO);
        if(spuResult.getCode()==200){

            if (spuResult.getData().size() == 1) {
                //spu信息
                SpuDTO spuInfo = spuResult.getData().get(0);
                map.put("spuInfo",spuInfo);

                //spu detail
                Result<SpuDetailEntity> detailResult = goodsFeign.getDetailBySpuId(spuId);
                if(detailResult.getCode()==200){
                    SpuDetailEntity spuDetailInfo = detailResult.getData();
                    map.put("spuDetailInfo",spuDetailInfo);
                }

                //查询分类信息
                List<String> strings = Arrays.asList(
                        spuInfo.getCid1() + ""
                        , spuInfo.getCid2() + ""
                        , spuInfo.getCid3() + "");
                Result<List<CategoryEntity>> cateListResult = cateGoryFeign
                        .getCategoryByIdList(String.join(",",strings));
                if (cateListResult.getCode()==200) {
                    map.put("cidList",cateListResult.getData());
                }

                //品牌信息
                BrandDTO brandDTO = new BrandDTO();
                brandDTO.setId(spuInfo.getBrandId());
                Result<PageInfo<BrandEntity>> brandInfoResult = brandFeign.getBrandInfo(brandDTO);
                if(brandInfoResult.getCode()==200){
                    PageInfo<BrandEntity> brandInfo = brandInfoResult.getData();
                    if(brandInfo.getList().size()==1){
                        map.put("brandInfo",brandInfo.getList().get(0));
                    }
                }
                //规格组 规格参数
                SpecGroupDTO specGroupDTO = new SpecGroupDTO();
                specGroupDTO.setCid(spuInfo.getCid3());
                Result<List<SpecGroupEntity>> specGroupResult = specificationfenign.getSpecGroupInfo(specGroupDTO);
                if(specGroupResult.getCode()==200){
                    List<SpecGroupEntity> specGroupList = specGroupResult.getData();
                    List<Object> gorpParamList = specGroupList.stream().map(specGroup -> {
                        //转换成 DTO
                        SpecGroupDTO sgd = BaiduBeanUtil.copyProperties(specGroup, SpecGroupDTO.class);

                        SpecParamDTO specParamDTO = new SpecParamDTO();
                        specParamDTO.setGroupId(sgd.getId());
                        specParamDTO.setGeneric(true);  //通用参数
                        Result<List<SpecParamEntity>> specParamResult = specificationfenign.getSpecParamInfo(specParamDTO);
                        if (specParamResult.getCode() == 200) {
                            sgd.setSpecParams(specParamResult.getData());
                        }
                        return sgd;
                    }).collect(Collectors.toList());
                    map.put("gorpParamList",gorpParamList);
                }

                //特有规格参数
                SpecParamDTO specParamDTO = new SpecParamDTO();
                specParamDTO.setCid(spuInfo.getCid3());
                specParamDTO.setGeneric(false); //不是通用属性
                Result<List<SpecParamEntity>> specParamInfoResult = specificationfenign.getSpecParamInfo(specParamDTO);
                if(specParamInfoResult.getCode()==200){
                    Map<Integer, String> specHashMap = new HashMap<>();

                    specParamInfoResult.getData().stream().forEach(param->specHashMap.put(param.getId(),param.getName()) );
                    map.put("specHashMap",specHashMap);
                }

                //sku信息
                Result<List<SkuDTO>> skuResult = goodsFeign.getSkuBySpuId(spuId);
                if(skuResult.getCode()==200){
                    map.put("skuInfo",skuResult.getData());
                }

            }
        }

        return map;
    }
}
