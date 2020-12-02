package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;

import com.baidu.shop.mapper.SpuMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService {

    @Resource
    private BrandMapper brandMapper;
    @Resource
    private CategoryBrandMapper categoryBrandMapper;
    @Resource
    private SpuMapper spuMapper;

    //通过brandid获取brand详细数据
    @Override
    public Result<List<BrandEntity>> getByBrandIdList(String brandsStr) {

        List<Integer> brandIdList = Arrays.asList(brandsStr.split(","))
                .stream().map(brandStr -> Integer.parseInt(brandStr)).collect(Collectors.toList());

        List<BrandEntity> list = brandMapper.selectByIdList(brandIdList);
        return this.setResultSuccess(list);
    }

    @Override
    public Result<BrandEntity> getBrandByCid(Integer cid) {

        List<BrandEntity> list = brandMapper.getBrendBycid(cid);

        return this.setResultSuccess(list);
    }

    //查询
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {

        //分页
        if(ObjectUtil.isNotNull(brandDTO.getPage()) && ObjectUtil.isNotNull(brandDTO.getRows()))
            PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());
        //排序
        Example example = new Example(BrandEntity.class);
        if(ObjectUtil.isNotNull(brandDTO.getOrder())){
            example.setOrderByClause(brandDTO.getOrderByClause());
        }

        Example.Criteria criteria = example.createCriteria();
        if(!StringUtils.isEmpty(brandDTO.getName())) criteria.andLike("name","%"+ brandDTO.getName() +"%");
        if(!StringUtils.isEmpty(brandDTO.getId()))  criteria.andEqualTo("id",brandDTO.getId());

        List<BrandEntity> list = brandMapper.selectByExample(example);


        PageInfo<BrandEntity> pageInfo = new PageInfo<>(list);
        return this.setResultSuccess(pageInfo);
    }

    @Transactional
    @Override
    public Result<JsonObject> saveBrandInfo(BrandDTO brandDTO) {

        //将brandDTO 转换成 BrandEntity
        BrandEntity entity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);

        //获取汉字首字母或全拼大写字母
        String name = brandDTO.getName();
        char c = name.charAt(0);   //获取第一个字符
        //  转换成大写             将char转换成String   方法中的参数  首字母大写
        String upperCase = PinyinUtil.getUpperCase(String.valueOf(c), PinyinUtil.TO_FIRST_CHAR_PINYIN);
        entity.setLetter(upperCase.charAt(0));

        // 新增品牌 并且可以返回主键
        brandMapper.insertSelective(entity);

        // 绑定中间表关系
        this.insertCategoryAndBrand(brandDTO, entity);


        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JsonObject> editBrandInfo(BrandDTO brandDTO) {
        //获取汉字首字母或拼音大写
        BrandEntity entity = BaiduBeanUtil.copyProperties(brandDTO, BrandEntity.class);
        entity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandDTO.getName().charAt(0))
                , PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        //执行修改操作
        brandMapper.updateByPrimaryKeySelective(entity);

        //通过 brandId 去删除中间表之间的关系
       this.deleteCategoryAndBrand(brandDTO.getId());

        //批量新增关系表中的数据
        insertCategoryAndBrand(brandDTO,entity);


        return setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JsonObject> deleteBrand(Integer brandId) {

        List<SpuEntity> spuList = spuMapper.getSpuByBrandId(brandId);
        if(spuList.size()!=0){
            return this.setResultError("品牌信息绑定了spu信息，不能删除");
        }

        //删除品牌信息
        brandMapper.deleteByPrimaryKey(brandId);

        //删除中间表
        this.deleteCategoryAndBrand(brandId);

        return setResultSuccess();
    }

    private void deleteCategoryAndBrand(Integer brandId){
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",brandId);
        categoryBrandMapper.deleteByExample(example);
    }


    //@Transactional
    private void insertCategoryAndBrand(BrandDTO brandDTO, BrandEntity brandEntity){

        if(brandDTO.getCategory().contains(",")){

            List<CategoryBrandEntity> categoryBrandEntities = Arrays.asList(brandDTO.getCategory().split(","))
                    .stream().map(cid -> {

                        CategoryBrandEntity entity = new CategoryBrandEntity();
                        entity.setCategoryId(Integer.parseInt(cid));
                        entity.setBrandId(brandEntity.getId());

                        return entity;
                    }).collect(Collectors.toList());

            categoryBrandMapper.insertList(categoryBrandEntities);
        }else{

            CategoryBrandEntity entity = new CategoryBrandEntity();
            entity.setCategoryId(StringUtil.toInteger(brandDTO.getCategory()));
            entity.setBrandId(brandEntity.getId());

            categoryBrandMapper.insertSelective(entity);
        }
    }



}
