package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lxn
 * @create 2020-03-23 23:07
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    //上架商品列表
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //查询sku对应的平台属性
        List<BaseAttrInfo> baseAttrInfoList = productFeignClient.getAttrList(skuId);
        if (null != baseAttrInfoList) {
            List<SearchAttr> searchAttrList = baseAttrInfoList.stream().map(baseAttrInfo -> {
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());

                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
              //  System.out.println(attrValueList.get(0).getValueName());
                searchAttr.setAttrValue(attrValueList.get(0).getValueName());

                return searchAttr;
            }).collect(Collectors.toList());
            goods.setAttrs(searchAttrList);
        }

        //获取品牌信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
        if (trademark != null) {
            goods.setTmId(trademark.getId());

            goods.setTmName(trademark.getTmName());

            goods.setTmLogoUrl(trademark.getLogoUrl());
        }

        //查询fenl
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        if (baseCategoryView != null) {
            goods.setCategory1Id(baseCategoryView.getCategory1Id());
            goods.setCategory1Name(baseCategoryView.getCategory1Name());
            goods.setCategory2Id(baseCategoryView.getCategory2Id());
            goods.setCategory2Name(baseCategoryView.getCategory2Name());
            goods.setCategory3Id(baseCategoryView.getCategory3Id());
            goods.setCategory3Name(baseCategoryView.getCategory3Name());
        }

        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setId(skuInfo.getId());
        goods.setTitle(skuInfo.getSkuName());
        goods.setCreateTime(new Date());

        this.goodsRepository.save(goods);

    }


    //下架商品列表
    @Override
    public void lowerGoods(Long skuId) {
        this.goodsRepository.deleteById(skuId);
    }

    //更新热点
    @Override
    public void incrHotScore(Long skuId) {
        String hotKey = "hotScore";

        Double hotScore = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);

        if (hotScore % 10 == 0) {
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(Math.round(hotScore));

            goodsRepository.save(goods);
        }


    }


    //搜索列表
    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {

        //构建del语句
        SearchRequest searchRequest = buildQueryDsl(searchParam);
        SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);//执行语句
      //  System.out.println(response);
        //返回结果集
        SearchResponseVo responseVo = this.parseSearchResult(response);

        responseVo.setPageSize(searchParam.getPageSize());
        responseVo.setPageNo(searchParam.getPageNo());
        Long totalPage = (responseVo.getTotal() + searchParam.getPageSize() - 1) / searchParam.getPageSize();
        responseVo.setTotalPages(totalPage);


        return responseVo;
    }


    //返回结果集
    private SearchResponseVo parseSearchResult(SearchResponse response) {

        SearchResponseVo searchResponseVo = new SearchResponseVo();
        Map<String, Aggregation> stringAggregationMap = response.getAggregations().asMap();
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) stringAggregationMap.get("tmIdAgg");

        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map(bucket -> {
            //声明一个品牌对象
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            searchResponseTmVo.setTmId(Long.parseLong(((Terms.Bucket) bucket).getKeyAsString()));
            // 获取品牌的名称
            Map<String, Aggregation> tmIdSubMap = ((Terms.Bucket) bucket).getAggregations().asMap();


            ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmIdSubMap.get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) tmIdSubMap.get("tmLogoUrlAgg");

            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            return searchResponseTmVo;
        }).collect(Collectors.toList());

        searchResponseVo.setTrademarkList(trademarkList);

        //商品
        List<Goods> goodsList = new ArrayList<>();
        //命中数据
        SearchHits hits = response.getHits();
        SearchHit[] hits1 = hits.getHits();
        if (hits1 != null && hits1.length > 0) {
            for (SearchHit subHit : hits1) {
                Goods goods = JSONObject.parseObject(subHit.getSourceAsString(), Goods.class);
                if (subHit.getHighlightFields().get("title") != null) {
                    Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                    goods.setTitle(title.toString());
                }
                goodsList.add(goods);
            }
        }
        //赋值商品数据
        searchResponseVo.setGoodsList(goodsList);

        //平台属性赋值
        ParsedNested attrAgg = (ParsedNested) stringAggregationMap.get("attrAgg");
//注意  只有ParsedLongTerms才有buckets
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)) {
            List<SearchResponseAttrVo> responseAttrVoList = buckets.stream().map(bucket -> {
                // 返回的平台属性对象
               // System.out.println("---------     bucket---------"+((Terms.Bucket) bucket).toString());
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                searchResponseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
//获取平台属性值
                ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = attrNameAgg.getBuckets();
               // System.out.println("nameAggBuckets:"+nameAggBuckets.get(0).getKeyAsString());
                searchResponseAttrVo.setAttrName(nameAggBuckets.get(0).getKeyAsString());
                // 获取平台属性值数据 Aggregation -->ParsedStringTerms

                ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");

               // System.out.println("-------------" + attrValueAgg);
                List<? extends Terms.Bucket> valueAggBuckets = attrValueAgg.getBuckets();

                List<String> attrValueList = valueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                searchResponseAttrVo.setAttrValueList(attrValueList);
                return searchResponseAttrVo;
            }).collect(Collectors.toList());
            searchResponseVo.setAttrsList(responseAttrVoList);

        }
        searchResponseVo.setTotal(hits.totalHits);


        return searchResponseVo;
    }

    //构建dsl 语句
    private SearchRequest buildQueryDsl(SearchParam searchParam) {

        //构建查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //boolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //判断检索的关键字
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            MatchQueryBuilder title = QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND);
            boolQueryBuilder.must(title);
        }
        //品牌查询
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {

            //截取
            String[] split = StringUtils.split(trademark, ":");
            if (split != null && split.length == 2) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", split[0]));

            }
        }

        if (null != searchParam.getCategory1Id())
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));

        if (null != searchParam.getCategory2Id())
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));

        if (null != searchParam.getCategory3Id())
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));


        //构建平台属性查询
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {

                String[] split = StringUtils.split(prop, ":");
                if (split != null && split.length == 3) {
                    //构建嵌套查询
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    //嵌套查询子查询
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();

                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));

                    boolQuery.must(QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None));
                    boolQueryBuilder.filter(boolQuery);
                }

            }
        }
        //执行查询方法
        searchSourceBuilder.query(boolQueryBuilder);
        //构建分页
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());

        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
            String[] split = StringUtils.split(order, ":");
            if (split != null && split.length == 2) {
                //排序的字段
                String field = null;
                switch (split[0]) {
                    case "1":
                        field = "hotScore";
                        break;
                    case "2":
                        field = "price";
                        break;
                }
                searchSourceBuilder.sort(field, "asc".equals(split[1]) ? SortOrder.ASC : SortOrder.DESC);
            } else {
                searchSourceBuilder.sort("hotScore", SortOrder.DESC);
            }
        }
        //构建高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.postTags("</span>");
        highlightBuilder.preTags("<span style=color:red>");

        searchSourceBuilder.highlighter(highlightBuilder);

        //设置品牌聚合
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        //设置平台聚会
//        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
//                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
//                  .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName")
//                 .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")))));


        // 2.   聚合平台属性
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        //结果集过滤  ????
        searchSourceBuilder.fetchSource(new String[]{"id", "defaultImg", "title", "price"}, null);//???
        SearchRequest request = new SearchRequest("goods");
        request.types("info");
        request.source(searchSourceBuilder);
        System.out.println("dsl:" + searchSourceBuilder.toString());


        return request;
    }
}
