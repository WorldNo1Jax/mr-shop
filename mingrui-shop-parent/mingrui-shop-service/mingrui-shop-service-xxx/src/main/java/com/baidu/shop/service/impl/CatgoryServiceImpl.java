package com.baidu.shop.service.impl;


import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.service.CategoryService;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CatgoryServiceImpl extends BaseApiService implements CategoryService {

   @Resource
   private CategoryMapper categoryMapper;
   @Resource
   private SpecGroupMapper specGroupMapper;
   @Resource
   private CategoryBrandMapper categoryBrandMapper;
   @Resource
   private BrandMapper brandMapper;

    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity entity = new CategoryEntity();

        //根据 父ID 去查商品
        entity.setParentId(pid);
        List<CategoryEntity> list = categoryMapper.select(entity);

        return this.setResultSuccess(list);
    }

    //新增
    @Transactional
    @Override
    public Result<JsonObject> saveCategoryByPid(CategoryEntity entity) {

        //新增时把 父节点的isParent 修改成 1
        CategoryEntity entity1 = new CategoryEntity();
        entity1.setId(entity.getParentId());
        entity1.setIsParent(1);
        categoryMapper.updateByPrimaryKeySelective(entity1);

        //新增
        categoryMapper.insertSelective(entity);

        return this.setResultSuccess();
    }

    //修改
    @Transactional
    @Override
    public Result<JsonObject> editCategoryByPid(CategoryEntity entity) {

        //修改
        categoryMapper.updateByPrimaryKeySelective(entity);

        return this.setResultSuccess();
    }

    //删除
    @Transactional
    @Override
    public Result<JsonObject> DeleteCategoryByPid(Integer Id) {


        //当前 Id 是否有值
        CategoryEntity entity = categoryMapper.selectByPrimaryKey(Id);
        if (null == entity) {
            return this.setResultError("Id不存在");
        }
        //判断当前是否为父节点 不删除父节点
        if (entity.getIsParent()==1) {
            return this.setResultError("不能删除父节点");
        }

        //查看 parentId 的值
        //初始化查询条件，需指定要操作的pojo实体类
        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId", entity.getParentId());

        List<CategoryEntity> list = categoryMapper.selectByExample(example);

        //判断当前分类信息 是否被品牌和分组 绑定
        String pName = "";
        String gName = "";
        Example example1 = new Example(CategoryBrandEntity.class);
        example1.createCriteria().andEqualTo("categoryId",Id);
        List<CategoryBrandEntity> categoryBrandList = categoryBrandMapper.selectByExample(example1);

        if(categoryBrandList.size()!=0){
            for (CategoryBrandEntity categoryBrandEntity : categoryBrandList) {
                BrandEntity entity1 = brandMapper.selectByPrimaryKey(categoryBrandEntity.getBrandId());
                pName +=  "'"+entity1.getName()+"'";
            }

            return this.setResultError("该分类被 "+ pName +" 品牌绑定，不能删除");
        }


        Example example2 = new Example(SpecGroupEntity.class);
        example2.createCriteria().andEqualTo("cid",Id);
        List<SpecGroupEntity> specGroupList = specGroupMapper.selectByExample(example2);
        if(specGroupList.size()!=0){
            for (SpecGroupEntity groupEntity : specGroupList) {
                gName += "'"+ groupEntity.getName() + "'";
            }

            return this.setResultError("该分类有 " +gName + " 分组信息绑定，不能删除");
        }


        // 如果只有 parentId 只有一个 就把父节点的isParent 变成 0
        if (list.size()==1) {
            CategoryEntity categoryEntity = new CategoryEntity();
            categoryEntity.setId(list.get(0).getParentId());
            categoryEntity.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(categoryEntity);
        }
        categoryMapper.deleteByPrimaryKey(Id);

        return this.setResultSuccess();
    }

    @Override
    public Result<List<CategoryEntity>> getByBrandId(Integer brandId) {

        List<CategoryEntity> list = categoryMapper.getByBrandId(brandId);

        return this.setResultSuccess(list);
    }

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Result<List<CategoryEntity>> getCategoryByIdList(String cidsStr) {

        System.out.println(cidsStr);
        List<Integer> cateIdList = Arrays.asList(cidsStr.split(",")).stream().
                map(cidStr -> Integer.parseInt(cidStr)).collect(Collectors.toList());

        List<CategoryEntity> list = categoryMapper.selectByIdList(cateIdList);
        return this.setResultSuccess(list);
    }
}
