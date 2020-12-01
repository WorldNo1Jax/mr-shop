package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.fenign.BrandFeign;
import com.baidu.shop.fenign.CateGoryFeign;
import com.baidu.shop.fenign.GoodsFeign;
import com.baidu.shop.fenign.Specificationfenign;
import com.baidu.shop.repository.GoodsRepository;
import com.baidu.shop.response.GoodsResponse;
import com.baidu.shop.service.ShopElasticsearchService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ESHighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ShopElasticsearchServiceImpl
 * @Description: TODO
 * @Author ljc
 * @Date 2020/9/16
 * @Version V1.0
 **/
@RestController
@Slf4j
public class ShopElasticsearchServiceImpl extends BaseApiService implements ShopElasticsearchService {


    @Resource
    private GoodsFeign goodsFeign;
    @Resource
    private Specificationfenign specificationfenign;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Resource
    private GoodsRepository goodsRepository;

    @Resource
    private BrandFeign brandFeign;
    @Resource
    private CateGoryFeign cateGoryFeign;



    @Override
    public GoodsResponse getSearch(String search,Integer page,String filter) {
        if(StringUtil.isEmpty(search)) throw new RuntimeException("请输入信息");

        //条件查询的结果
        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.
                search(this.getNativeSearchQueryBuilder(search, page,filter).build(), GoodsDoc.class);
        //把返回的值替换成高亮
        List<SearchHit<GoodsDoc>> searchHits1 = ESHighLightUtil.getHighLightHit(searchHits.getSearchHits());
        //返回的商品集合
        List<GoodsDoc> goodsDocs = searchHits1.stream().map(searchHit -> searchHit.getContent())
                .collect(Collectors.toList());

        //总条数&总页数
        long total = searchHits.getTotalHits();
        Long totalPage = Double.valueOf(Math.ceil(Long.valueOf(total).doubleValue() / 10)).longValue();

        //获取聚合数据
        Aggregations aggregations = searchHits.getAggregations();
        Map<Integer, List<CategoryEntity>> map = this.getCateListResult(aggregations);
        List<CategoryEntity> categoryList =null;
        Integer hotCid = null;
        //注意 此处不能使用forEach
        for(Integer key:map.keySet()){
            hotCid = key;
            categoryList = map.get(key);
        }

        Map<String, List<String>> specParamMap = this.getSpecParamList(hotCid,search);

        List<BrandEntity> brandIDList = this.getBrandListResult(aggregations);

        return new GoodsResponse(total,totalPage,categoryList,brandIDList,goodsDocs,specParamMap);
    }


    //

    /**
     * 获得规格参数的数据集合
     * @param hotCid  分类Id
     * @param search  搜索的字段
     * @return  key 规格参数Name
     *          value 查询规格参数的数据
     */
    private Map<String, List<String>> getSpecParamList(Integer hotCid,String search){
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(hotCid);
        specParamDTO.setSearching(true); //只查询搜索字段
        Result<List<SpecParamEntity>> specParamInfo = specificationfenign.getSpecParamInfo(specParamDTO);
        if(specParamInfo.getCode()==200){
            List<SpecParamEntity> specParamList = specParamInfo.getData();

            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"title","brandName","categoryName"));

            specParamList.stream().forEach(params -> {
                //聚合
                queryBuilder.addAggregation(AggregationBuilders.terms(params.getName()).field("specs." + params.getName() + ".keyword"));
            });
            queryBuilder.withPageable(PageRequest.of(0,1));

            SearchHits<GoodsDoc> hits = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsDoc.class);
            Aggregations aggregations = hits.getAggregations(); //获得聚合数据

            Map<String, List<String>> map = new HashMap<>();

            specParamList.stream().forEach(specParam->{
                Terms terms = aggregations.get(specParam.getName());
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                //value是查询处来的数据
                List<String> value = buckets.stream().map(bucket -> bucket.getKeyAsString())
                        .collect(Collectors.toList());
                map.put(specParam.getName(),value);
            });
            return map;
        }
        return null;
    }

    //构建条件查询
    private NativeSearchQueryBuilder getNativeSearchQueryBuilder(String search, Integer page,String filter) {

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //构建查询字段    多字段同事查询
        if(StringUtil.isNotEmpty(search)){
            queryBuilder.withQuery(
                    QueryBuilders.multiMatchQuery(search,"title","brandName","categoryName")
            );
        }
        // 过滤查询
        if(StringUtil.isNotEmpty(filter)&& filter.length()>2){
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            //把 map字符串转换成Map
            Map<String, String> filterMap = JSONUtil.toMapValueString(filter);

            for(Map.Entry<String,String> item : filterMap.entrySet()){
                MatchQueryBuilder matchQueryBuilder = null;
                //分类 品牌和 规格参数的查询方式不一样
                if(item.getKey().equals("cid3") || item.getKey().equals("brandId")){
                    matchQueryBuilder = QueryBuilders.matchQuery(item.getKey(), item.getValue());
                }else{
                    matchQueryBuilder = QueryBuilders.matchQuery("specs." + item.getKey() + ".keyword",item.getValue());
                }
                boolQueryBuilder.must(matchQueryBuilder);
            }
            //添加过滤,过滤不会影响评分
            queryBuilder.withFilter(boolQueryBuilder);
        }


        //设置高亮字段
        queryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));
        //分页
        queryBuilder.withPageable(PageRequest.of(page-1,10));
        // 通过cid3 聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("cate_agg").field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms("brand_agg").field("brandId"));

        return queryBuilder;
    }

    //获取分类的集合
    private HashMap<Integer, List<CategoryEntity>> getCateListResult(Aggregations aggregations) {
        HashMap<Integer, List<CategoryEntity>> map = new HashMap<>();

        Terms cid_agg = aggregations.get("cate_agg");

        List<? extends Terms.Bucket> cid_aggBuckets = cid_agg.getBuckets();
        List<Integer> hotCidList = Arrays.asList(0);  //热度最高的分类id
        List<Integer> maxContList = Arrays.asList(0);
        //返回一个id的集合-->通过id的集合去查询数据
        List<String> cidList = cid_aggBuckets.stream().map(cidBuckets ->{
            String cidAsString = cidBuckets.getKeyAsString();

            if(maxContList.get(0)< cidBuckets.getDocCount()){
                maxContList.set(0, Long.valueOf(cidBuckets.getDocCount()).intValue());
                hotCidList.set(0,Integer.parseInt(cidAsString));
            }

            return cidAsString;
        }).collect(Collectors.toList());

        //通过分类id集合去查询数据
        //将List集合转换成,分隔的string字符串
        // String.join(",", cidList); 通过,分隔list集合 --> 返回,拼接的string字符串
        String cidsStr = String.join(",", cidList);
        Result<List<CategoryEntity>> categoryByIdList = cateGoryFeign.getCategoryByIdList(cidsStr);

        map.put(hotCidList.get(0),categoryByIdList.getData()); //key 为热度最高的CId value是查询数据的集合
        return map;
    }
    //获取品牌的集合
    private List<BrandEntity> getBrandListResult(Aggregations aggregations) {
        Terms brand_agg = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> brand_aggBuckets = brand_agg.getBuckets();
        List<String> brandIdList = brand_aggBuckets.stream().map(brandBuckets -> {
            return brandBuckets.getKeyAsString();
        }).collect(Collectors.toList());
        //将品牌Id list转换成字符串
        return brandFeign.getByBrandIdList(String.join(",",brandIdList)).getData();
    }

    //创建索引
    @Override
    public Result<JSONObject> initGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(!indexOperations.exists()){
            indexOperations.create();

            log.info("索引创建成功");
            indexOperations.createMapping();
            log.info("映射创建成功");
        }

        //批量新增数据
        List<GoodsDoc> goodsDocs = this.esGoodsInfo(new SpuDTO());
        elasticsearchRestTemplate.save(goodsDocs);

        return this.setResultSuccess();
    }

    //新增数据到es库 创建索引
    @Override
    public Result<JSONObject> saveData(Integer spuId) {

        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);

        List<GoodsDoc> goodsDocs = this.esGoodsInfo(spuDTO);
        GoodsDoc goodsDoc = goodsDocs.get(0);
        elasticsearchRestTemplate.save(goodsDoc);

        return this.setResultSuccess();
    }

    //通过supID删除索引
    @Override
    public Result<JSONObject> delData(Integer spuId) {

        GoodsDoc goodsDoc = new GoodsDoc();
        goodsDoc.setId(spuId.longValue());

        elasticsearchRestTemplate.delete(goodsDoc);
        return this.setResultSuccess();
    }

    /**
     * 清空es数据
     * @return
     */
    @Override
    public Result<JSONObject> clearGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(indexOperations.exists()){
            indexOperations.delete();
            log.info("索引删除成功");
        }

        return this.setResultSuccess();
    }



    public List<GoodsDoc> esGoodsInfo(SpuDTO spuDTO) {
        //查询出来的数据是多个spu
        List<GoodsDoc> goodsDocs = new ArrayList<>();

        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);

        if(spuInfo.getCode() == HTTPStatus.OK){


            //spu数据
            List<SpuDTO> spuList = spuInfo.getData();


            spuList.stream().forEach(spu -> {
                GoodsDoc goodsDoc = new GoodsDoc();

                //spu的数据填充
                goodsDoc.setId(spu.getId().longValue());
                goodsDoc.setTitle(spu.getTitle());
                goodsDoc.setBrandId(spu.getBrandId().longValue());
                goodsDoc.setBrandName(spu.getBrandName());
                goodsDoc.setSubTitle(spu.getSubTitle());
                goodsDoc.setCategoryName(spu.getCategoryName());
                goodsDoc.setCid1(spu.getCid1().longValue());
                goodsDoc.setCid2(spu.getCid2().longValue());
                goodsDoc.setCid3(spu.getCid3().longValue());
                goodsDoc.setCreateTime(spu.getCreateTime());

                //通过spuId 查出skuList
                Map<List<Long>, List<Map<String, Object>>> skus = this.getSkusAndPriceList(spu.getId());
                skus.forEach((k,v)->{
                    goodsDoc.setPrice(k);
                    goodsDoc.setSkus(JSONUtil.toJsonString(v));
                });

                //获取规格参数填充  通过cid3 去查规格参数
                HashMap<String, Object> specMap = this.getSpecMap(spu);

                goodsDoc.setSpecs(specMap);
                System.out.println(goodsDoc);
                goodsDocs.add(goodsDoc);

            });
            System.out.println(goodsDocs);
        }

        return goodsDocs;
    }

    private Map<List<Long>, List<Map<String, Object>>> getSkusAndPriceList(Integer spuId){

        Map<List<Long>, List<Map<String, Object>>> hashMap = new HashMap<>();
        Result<List<SkuDTO>> skuResult  = goodsFeign.getSkuBySpuId(spuId);

        List<Map<String, Object>> skuMap = null;
        List<Long> priceList = new ArrayList<>();

        if(skuResult.getCode()==HTTPStatus.OK){
            List<SkuDTO> skusList = skuResult.getData();

            skuMap = skusList.stream().map(sku -> {
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", sku.getId());
                map.put("title", sku.getTitle());
                map.put("images", sku.getImages());
                map.put("price", sku.getPrice());

                priceList.add(sku.getPrice().longValue());
                return map;
            }).collect(Collectors.toList());
        }
        hashMap.put(priceList,skuMap);
        return hashMap;
    }


    private HashMap<String, Object> getSpecMap(SpuDTO spuDTO){
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(spuDTO.getCid3());

        Result<List<SpecParamEntity>> specParamInfo = specificationfenign.getSpecParamInfo(specParamDTO);

        HashMap<String, Object> specMap = new HashMap<>();

        if (specParamInfo.getCode()==HTTPStatus.OK){
            List<SpecParamEntity> specParamList = specParamInfo.getData();
            //通过spuId去查询spuDetail
            Result<SpuDetailEntity> spuDetailResult= goodsFeign.getDetailBySpuId(spuDTO.getId());

            if(spuDetailResult.getCode()==HTTPStatus.OK){
                SpuDetailEntity spuDetailInfo = spuDetailResult.getData();

                //通用规格参数的值
                String genericSpec = spuDetailInfo.getGenericSpec();
                Map<String, String> genericSpecMap  = JSONUtil.toMapValueString(genericSpec);
                //特有规格参数的值
                String specialSpec = spuDetailInfo.getSpecialSpec();
                Map<String, List<String>> specialSpecMap = JSONUtil.toMapValueStrList(specialSpec);

                specParamList.stream().forEach(param->{
                    if(param.getGeneric()){     //是否为sku的通用属性
                        if(param.getNumeric() && param.getSearching()){ //是否是数字类型 是否是搜索字段
                            specMap.put(param.getName(),this.chooseSegment(genericSpecMap.get(param.getId()+""),param.getSegments(),param.getUnit()));
                        }else{
                            specMap.put(param.getName(),genericSpecMap.get(param.getId()+""));  // key是字符串 所以 +“ ”
                        }
                    }else {
                        specMap.put(param.getName(),specialSpecMap.get(param.getId() + ""));
                    }
                });
            }
        }

        return specMap;
    }



    /**
     * 把具体的值转换成区间-->不做范围查询
     * @param value
     * @param segments
     * @param unit
     * @return
     */
    private String chooseSegment(String value, String segments, String unit) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : segments.split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + unit + "以上";
                }else if(begin == 0){
                    result = segs[1] + unit + "以下";
                }else{
                    result = segment + unit;
                }
                break;
            }
        }
        return result;
    }
}
