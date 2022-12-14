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
     * ??????
     *
     * @param skuId
     * @return void
     * @author SongBoHao
     * @date 2022/9/6 18:41
     */
    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();
        //??????sku??????
        SkuInfo skuInfo = this.productFeignClient.getSkuInfo(skuId);
        //??????
        goods.setId(skuInfo.getId());
        goods.setTitle(skuInfo.getSkuName());
        goods.setPrice(productFeignClient.getSkuPrice(skuId).doubleValue());
        goods.setCategory3Id(skuInfo.getCategory3Id());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        //??????????????????????????????
        BaseCategoryView categoryView = this.productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        //??????????????????
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Name(categoryView.getCategory3Name());
        //??????????????????????????????
        BaseTrademark tradeMark = this.productFeignClient.getTradeMark(skuInfo.getTmId());
        goods.setTmId(tradeMark.getId());
        goods.setTmName(tradeMark.getTmName());
        goods.setTmLogoUrl(tradeMark.getLogoUrl());

        //??????sku?????????????????????
        List<BaseAttrInfo> attrList = this.productFeignClient.getAttrList(skuInfo.getId());
        List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
            SearchAttr searchAttr = new SearchAttr();
            searchAttr.setAttrId(baseAttrInfo.getId());
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            //??????sku????????????????????????
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            return searchAttr;
        }).collect(Collectors.toList());
        //??????
        goods.setAttrs(searchAttrList);
        //????????????
        goods.setCreateTime(new Date());
        //?????????es
        this.goodsRepository.save(goods);
    }

    /**
     * ??????
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
     * ????????????
     *
     * @param skuId
     */
    @Override
    public void incrHotScore(Long skuId) {
        // ??????key
        String hotKey = "hotScore";
        // ????????????
        Double count = redisTemplate.opsForZSet().incrementScore(hotKey, "skuId:" + skuId, 1);
        if (count % 10 == 0) {
            Optional<Goods> goodsOptional = this.goodsRepository.findById(skuId);
            Goods goods = goodsOptional.get();
            goods.setHotScore(count.longValue());
            this.goodsRepository.save(goods);
        }
    }

    /**
     * ??????
     *
     * @param searchParam
     */
    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {
        /*
        * ?????? ????????????dsl??????
        * 1??? ??????dsl??????
        * 2??? ??????dsl??????
        * 3??? ?????????????????????????????????SearchResponseVo
        * */
        //??????????????????????????????
        SearchRequest searchRequest = this.bulidDsl(searchParam);
        //??????dsl??????
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //?????????????????????????????????SearchResponseVo
        SearchResponseVo searchResponseVo = this.parseResult(searchResponse);
        //?????????????????????????????????
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //?????????????????????
        searchResponseVo.setPageNo(searchParam.getPageNo());
        Long totalPages = (searchResponseVo.getTotal() + searchResponseVo.getPageSize() - 1) / searchResponseVo.getPageSize();
        searchResponseVo.setTotalPages(totalPages);
        //????????????
        return searchResponseVo;
    }

    /**
     * ??????????????????
     *
     * @param searchResponse
     */
    private SearchResponseVo parseResult(SearchResponse searchResponse) {
        //????????????
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        //1?????????????????????
        SearchHits hits = searchResponse.getHits();
        searchResponseVo.setTotal(hits.getTotalHits().value);
        //2?????????????????????
        SearchHit[] subHits = hits.getHits();
        if (subHits != null&& subHits.length > 0) {
            //??????????????????
            /*for (SearchHit subHit : subHits) {
                //?????????source?????????
                String sourceAsString = subHit.getSourceAsString();
                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            }*/
            List<Goods> goodsList = Arrays.asList(subHits).stream().map(subHit -> {
                String sourceAsString = subHit.getSourceAsString();
                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
                //?????????????????????????????????????????????
                if (subHit.getHighlightFields().get("title") != null) {
                    //??????????????????????????????
                    Text[] titles = subHit.getHighlightFields().get("title").getFragments();
                    goods.setTitle(titles[0].toString());
                }
                return goods;
            }).collect(Collectors.toList());
            searchResponseVo.setGoodsList(goodsList);
        }
        //??????????????????
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        //????????????????????? ????????? List<SearchResponseTmVo> trademarkList;
        List<SearchResponseTmVo> searchResponseTmVoList = tmIdAgg.getBuckets().stream().map(bucket -> {
            //??????????????????
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //????????????id?????????
            searchResponseTmVo.setTmId(Long.parseLong(bucket.getKeyAsString()));

            //??????????????????
            Map<String, Aggregation> tmNameMap = bucket.getAggregations().getAsMap();
            //???????????????
            ParsedStringTerms tmNameAgg = (ParsedStringTerms) tmNameMap.get("tmNameAgg");
            //???????????????
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);

            //????????????logoUrl
            Map<String, Aggregation> asMap = bucket.getAggregations().getAsMap();
            //???????????????
            ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) asMap.get("tmLogoUrlAgg");
            //???????????????
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            return searchResponseTmVo;
        }).collect(Collectors.toList());
        searchResponseVo.setTrademarkList(searchResponseTmVoList);

        //?????????????????????
        ParsedNested attrAgg = (ParsedNested) searchResponse.getAggregations().getAsMap().get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> searchResponseAttrVoList = attrIdAgg.getBuckets().stream().map(bucket -> {
            //????????????????????????
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            //??????????????????id?????????
            searchResponseAttrVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
            //??????????????????name
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseAttrVo.setAttrName(attrName);

            //??????????????????valueList
            ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
            List<String> valueList = attrValueAgg.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            searchResponseAttrVo.setAttrValueList(valueList);
            return searchResponseAttrVo;
        }).collect(Collectors.toList());
        searchResponseVo.setAttrsList(searchResponseAttrVoList);
        //????????????
        return searchResponseVo;
    }

    /**
     * ????????????dsl????????????????????????
     *
     * @param searchParam
     */
    private SearchRequest bulidDsl(SearchParam searchParam) {
        //???????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ??????boolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //????????????id??????
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id()));
        }
        //?????????????????????
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {

            boolQueryBuilder.must(QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND));
            // ????????????
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            // ??????????????????
            highlightBuilder.field("title").preTags("<span style=color:red>").postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        // ??????????????????
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            if (split != null && split.length == 2) {
                // ????????????Id??????
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }
        // ????????????????????????
        // ???????????????????????????
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                // ?????? ???????????????
                String[] split = prop.split(":");
                // ???????????????boolQueryBuilder
                BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
                // ????????????boolQueryBuilder
                BoolQueryBuilder innerBoolBuilder = QueryBuilders.boolQuery();
                innerBoolBuilder.must(QueryBuilders.matchQuery("attrs.attrId", split[0]));
                innerBoolBuilder.must(QueryBuilders.matchQuery("attrs.attrValue", split[1]));
                //?????????boolQueryBuilder???????????????boolQueryBuilder
                boolBuilder.must(QueryBuilders.nestedQuery("attrs", innerBoolBuilder, ScoreMode.None));
                //????????????boolQueryBuilder????????????boolQueryBuilder
                boolQueryBuilder.filter(boolBuilder);
            }
        }
        //query
        searchSourceBuilder.query(boolQueryBuilder);
        // ????????????
        int from = (searchParam.getPageNo() - 1) * searchParam.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());
        // ????????????
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
            //???????????????
            String[] split = order.split(":");
            //??????????????????
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
            //???????????? ??????????????????
            searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        }
        //????????????
        //??????????????????
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"))
        );
        //????????????????????????
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));
        //????????????????????????
        SearchRequest searchRequest = new SearchRequest();
        //???dsl???????????????????????????
        searchRequest.source(searchSourceBuilder);
        System.out.println("Dsl:\t" + searchSourceBuilder.toString());
        //??????????????????????????????
        searchSourceBuilder.fetchSource(new String[]{"id", "defaultImg", "title", "price", "createTime"}, null);
        return searchRequest;
    }
}
