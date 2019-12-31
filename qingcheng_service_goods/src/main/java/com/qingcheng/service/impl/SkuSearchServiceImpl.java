package com.qingcheng.service.impl;

import com.qingcheng.service.goods.SkuSearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SkuSearchServiceImpl implements SkuSearchService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public Map search(Map<String,String> searchMap) {
        /*封装查询请求*/
        /*设置索引和类型*/
        SearchRequest searchRequest = new SearchRequest("sku");
        searchRequest.types("doc");
        /*查询器 and 构建查询语句*/
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name",searchMap.get("keywords"));
        boolQueryBuilder.must(matchQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        /*处理结果*/
        Map resultMap = new HashMap();
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = search.getHits();
            long totalHits = hits.getTotalHits(); /*总数*/
            log.info("allNUm"+totalHits);
            SearchHit[] hits1 = hits.getHits();
            ArrayList<Map<String, Object>> resultList = new ArrayList<>();
            for (SearchHit hit:hits1){
                /*取到结果对象*/
                Map<String, Object> skumap = hit.getSourceAsMap();
                resultList.add(skumap);
            }
            resultMap.put("rows",resultList);
        }catch (IOException e){
            e.printStackTrace();
        }

        return resultMap;
    }
}
