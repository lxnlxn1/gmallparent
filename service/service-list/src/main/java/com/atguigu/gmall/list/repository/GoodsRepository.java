package com.atguigu.gmall.list.repository;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author lxn
 * @create 2020-03-23 23:04
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {



}
