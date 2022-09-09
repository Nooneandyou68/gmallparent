package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @PROJECT_NAME: gmallparent
 * @DESCRIPTION:
 * @USER: Administrator
 * @DATE: 2022/9/6 18:39
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
    /**
     * 上架
     *
     * @param skuId
     * @return void
     * @author SongBoHao
     * @date 2022/9/6 18:41
     */
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //查询sku信息
        SkuInfo skuInfo = this.productFeignClient.getSkuInfo(skuId);
        //赋值
        goods.setId(skuInfo.getId());
        goods.setTitle(skuInfo.getSkuName());
        goods.setPrice(productFeignClient.getSkuPrice(skuId).doubleValue());
        goods.setCategory3Id(skuInfo.getCategory3Id());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        //远程调用获取分类数据
        BaseCategoryView categoryView = this.productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        //赋值对应数据
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Name(categoryView.getCategory3Name());
        //远程调用查询品牌信息
        BaseTrademark tradeMark = this.productFeignClient.getTradeMark(skuInfo.getTmId());
        goods.setTmId(tradeMark.getId());
        goods.setTmName(tradeMark.getTmName());
        goods.setTmLogoUrl(tradeMark.getLogoUrl());

        //查询sku对应的平台属性
        List<BaseAttrInfo> attrList = this.productFeignClient.getAttrList(skuInfo.getId());
        List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            //一个sku只对应一个属性值
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            return searchAttr;
        }).collect(Collectors.toList());
        //赋值
        goods.setAttrs(searchAttrList);
        //上架时间
        goods.setCreateTime(new Date());
        //保存到es
        this.goodsRepository.save(goods);
    }

    /**
     * 下架
     *
     * @param
     * @return
     * @author SongBoHao
     * @date 2022/9/6 18:41
     */
    @Override
    public void lowerGoods(Long skuId) {
        this.goodsRepository.deleteById(skuId);
    }

    /**
     * 更新热点
     *
     * @param skuId
     */
    @Override
    public void incrHotScore(Long skuId) {
        // 定义key
        String hotKey = "hotScore";
        // 保存数据
        Double count = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);
        if (count % 10 == 0) {
            Optional<Goods> goodsOptional = this.goodsRepository.findById(skuId);
            Goods goods = goodsOptional.get();
            goods.setHotScore(count.longValue());
            this.goodsRepository.save(goods);
        }
    }

    /**
     * 检索
     *
     * @param searchParam
     */
    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {
        /*
        * 检索 动态生成dsl语句
        * 1、 生成dsl语句
        * 2、 执行dsl语句
        * 3、 将执行之后的结果封装到SearchResponseVo
        * */
        //声明一个查询请求对象
        SearchRequest searchRequest = this.bulidDsl(searchParam);
        //执行dsl语句
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //将执行之后的结果封装到SearchResponseVo
        SearchResponseVo searchResponseVo = this.parseResult(searchResponse);
        //默认设置每页显示的条数
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //默认当前第几页
        searchResponseVo.setPageNo(searchParam.getPageNo());
        Long totalPages = (searchResponseVo.getTotal() + searchResponseVo.getPageSize() - 1) / searchResponseVo.getPageSize();
        searchResponseVo.setTotalPages(totalPages);
        //返回数据
        return searchResponseVo;
    }

    /**
     * 设置返回数据
     *
     * @param searchResponse
     */
    private SearchResponseVo parseResult(SearchResponse searchResponse) {
        //声明对象
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        //1、设置总记录数
        SearchHits hits = searchResponse.getHits();
        searchResponseVo.setTotal(hits.getTotalHits().value);
        //2、设置商品集合
        SearchHit[] subHits = hits.getHits();
        if (subHits != null&& subHits.length > 0) {
            //遍历获取数据
            /*for (SearchHit subHit : subHits) {
                //获取到source字符串
                String sourceAsString = subHit.getSourceAsString();
                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            }*/
            List<Goods> goodsList = Arrays.asList(subHits).stream().map(subHit -> {
                String sourceAsString = subHit.getSourceAsString();
                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
                //判断用户是否根据关键词进行检索
                if (subHit.getHighlightFields().get("title") != null) {
                    //如果不为空说明有高亮
                    Text[] titles = subHit.getHighlightFields().get("title").getFragments();
                    goods.setTitle(titles[0].toString());
                }
                return goods;
            }).collect(Collectors.toList());
            searchResponseVo.setGoodsList(goodsList);
        }
        //设置品牌数据
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        //从桶中获取数据 转化为 List<SearchResponseTmVo> trademarkList;
        List<SearchResponseTmVo> searchResponseTmVoList = tmIdAgg.getBuckets().stream().map(bucket -> {
            //声明返回对象
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //获取品牌id并赋值
            searchResponseTmVo.setTmId(Long.parseLong(bucket.getKeyAsString()));

            //获取品牌名，
            Map<String, Aggregation> tmNameMap = bucket.getAggregations().getAsMap();
            //获取子聚合
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmNameMap.get("tmNameAgg");
            //获取桶数据
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);

            //获取品牌logoUrl
            Map<String, Aggregation> asMap = bucket.getAggregations().getAsMap();
            //获取子聚合
            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) asMap.get("tmLogoUrlAgg");
            //获取桶数据
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            return searchResponseTmVo;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(searchResponseTmVoList);

        //设置平台属性值
        ParsedNested attrAgg = (ParsedNested) searchResponse.getAggregations().getAsMap().get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> searchResponseAttrVoList = attrIdAgg.getBuckets().stream().map(bucket -> {
            //声明接收数据对象
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //获取平台属性id并赋值
            searchResponseAttrVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
            //获取平台属性name
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);

            //获取平台属性valueList
            ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
            List<String> valueList = attrValueAgg.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            searchResponseAttrVo.setAttrValueList(valueList);
            return searchResponseAttrVo;
        }).collect(Collectors.toList());
        searchResponseVo.setAttrsList(searchResponseAttrVoList);
        //返回数据
        return searchResponseVo;
    }

    /**
     * 动态生成dsl语句返回请求对象
     *
     * @param searchParam
     */
    private SearchRequest bulidDsl(SearchParam searchParam) {
        //创建查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构建boolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //构建分类id查询
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }
        //构建关键词查询
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {

            boolQueryBuilder.must(QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND));
            // 构建高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            // 设置高亮规则
            highlightBuilder.field("title").preTags("<span style=color:red>").postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        // 构建品牌查询
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            if (split != null && split.length == 2) {
                // 根据品牌Id过滤
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }
        // 构建平台属性查询
        // 先获取用户点击数据
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                // 遍历 字符串分割
                String[] split = prop.split(":");
                // 声明中间层boolQueryBuilder
                BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
                // 声明内层boolQueryBuilder
                BoolQueryBuilder innerBoolBuilder = QueryBuilders.boolQuery();
                innerBoolBuilder.must(QueryBuilders.matchQuery("attrs.attrId", split[0]));
                innerBoolBuilder.must(QueryBuilders.matchQuery("attrs.attrValue", split[1]));
                //把内层boolQueryBuilder放入中间层boolQueryBuilder
                boolBuilder.must(QueryBuilders.nestedQuery("attrs", innerBoolBuilder, ScoreMode.None));
                //把中间层boolQueryBuilder放入外层boolQueryBuilder
                boolQueryBuilder.filter(boolBuilder);
            }
        }
        //query
        searchSourceBuilder.query(boolQueryBuilder);
        // 构建分页
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());
        // 构建排序
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
            //分割字符串
            String[] split = order.split(":");
            //判断是否有值
            if (split != null && split.length == 2) {
                String field = "";
                switch (split[0]) {
                    case "1":
                        field = "hotScore";
                        break;
                    case "2":
                        field = "price";
                        break;
                }
                searchSourceBuilder.sort(field, "asc".equals(split[1]) ? SortOrder.ASC : SortOrder.DESC);
            }
        }else {
            //如果为空 设置默认排序
            searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        }
        //构建聚合
        //第一部分品牌
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"))
        );
        //第二部分平台属性
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));
        //声明一个请求对象
        SearchRequest searchRequest = new SearchRequest();
        //将dsl语句赋值给查询对象
        searchRequest.source(searchSourceBuilder);
        System.out.println("Dsl:\t" + searchSourceBuilder.toString());
        //设置不需要展示的数据
        searchSourceBuilder.fetchSource(new String[]{"id", "defaultImg", "title", "price", "createTime"}, null);
        return searchRequest;
    }
}
