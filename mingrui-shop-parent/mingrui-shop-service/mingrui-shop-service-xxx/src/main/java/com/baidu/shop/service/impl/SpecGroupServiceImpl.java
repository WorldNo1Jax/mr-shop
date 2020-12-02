package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.mapper.SpecGroupMapper;
import com.baidu.shop.mapper.SpecParamMapper;
import com.baidu.shop.service.SpecificationService;
import com.baidu.shop.utils.BaiduBeanUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;

/**
 * @ClassName SpecGroupServiceImpl
 * @Description: TODO
 * @Author ljc
 * @Date 2020/9/3
 * @Version V1.0
 **/
@RestController
public class SpecGroupServiceImpl extends BaseApiService implements SpecificationService {

    @Resource
    private SpecGroupMapper specGroupMapper;

    @Resource
    private SpecParamMapper specParamMapper;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Result<List<SpecGroupEntity>> getSpecGroupInfo(SpecGroupDTO specGroupDTO) {

        Example example = new Example(SpecGroupEntity.class);
        example.createCriteria().andEqualTo("cid",specGroupDTO.getCid());

        List<SpecGroupEntity> list = specGroupMapper.selectByExample(example);
        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JSONObject> saveSpecGroupInfo(SpecGroupDTO specGroupDTO) {

        specGroupMapper.insertSelective(BaiduBeanUtil.copyProperties(specGroupDTO, SpecGroupEntity.class));
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JSONObject> editSpecGroupInfo(SpecGroupDTO specGroupDTO) {

        specGroupMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specGroupDTO, SpecGroupEntity.class));
        return this.setResultSuccess();
    }

    //删除规格组
    @Transactional
    @Override
    public Result<JSONObject> delete(Integer id) {
        System.out.println(id);
        //通过groupId  去param表 查找是否绑定关系 true 不删除  提示被绑定 false 直接删除
        Example example = new Example(SpecParamEntity.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("groupId",id);

        List<SpecParamEntity> list = specParamMapper.selectByExample(example);
        if (list.size()==0){
            //没有绑定 直接删除
            specGroupMapper.deleteByPrimaryKey(id);
            return this.setResultSuccess();
        }

        //存在绑定关系 不能删除
        String name = "";
        HashSet<Object> nameSet = new HashSet<>();
        list.forEach(l ->{
            nameSet.add(l.getName());
        });
        for (Object o : nameSet) {
            name += o;
        }
        return this.setResultError("该组被" + name +"绑定不能删除");
    }

    //查询规格参数信息
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Override
    public Result<List<SpecParamEntity>> getSpecParamInfo(SpecParamDTO specParamDTO) {

        Example example = new Example(SpecParamEntity.class);
        Example.Criteria criteria = example.createCriteria();
        if(ObjectUtil.isNotNull(specParamDTO.getGroupId())){
            criteria.andEqualTo("groupId",specParamDTO.getGroupId());
        }
        if(ObjectUtil.isNotNull(specParamDTO.getCid())) {
            criteria.andEqualTo("cid",specParamDTO.getCid());
        }
        if(ObjectUtil.isNotNull(specParamDTO.getSearching())){
            criteria.andEqualTo("searching",specParamDTO.getSearching());
        }
        if(ObjectUtil.isNotNull(specParamDTO.getGeneric()))
            criteria.andEqualTo("generic",specParamDTO.getGeneric());

        List<SpecParamEntity> list = specParamMapper.selectByExample(example);
        return this.setResultSuccess(list);
    }

    //新增参数信息
    @Transactional
    @Override
    public Result<JsonObject> saveSpecParamInfo(SpecParamDTO specParamDTO) {

        specParamMapper.insertSelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));
        return this.setResultSuccess();
    }
    //修改参数信息
    @Transactional
    @Override
    public Result<JsonObject> editSpecParamInfo(SpecParamDTO specParamDTO) {
        specParamMapper.updateByPrimaryKeySelective(BaiduBeanUtil.copyProperties(specParamDTO,SpecParamEntity.class));
        return this.setResultSuccess();
    }
    //删除参数信息
    @Transactional
    @Override
    public Result<JsonObject> deleteParam(Integer id) {

        specParamMapper.deleteByPrimaryKey(id);
        return setResultSuccess();
    }

    //同过规格参数id 去名称
    @Override
    public String getOwnSpecName(String key) {

        String paramName= specParamMapper.getOwnSpecName(key);

       return paramName;
    }
}
