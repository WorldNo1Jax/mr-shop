package com.baidu.shop.repository;

import com.baidu.shop.document.GoodsDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @ClassName GoodsRepository
 * @Description: TODO
 * @Author ljc
 * @Date 2020/9/17
 * @Version V1.0
 **/
public interface GoodsRepository extends ElasticsearchRepository<GoodsDoc,Long> {

}
