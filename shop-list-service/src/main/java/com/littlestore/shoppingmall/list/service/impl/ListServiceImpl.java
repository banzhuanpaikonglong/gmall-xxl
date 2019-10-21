package com.littlestore.shoppingmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.littlestore.shoppingmall.bean.SkuLsInfo;
import com.littlestore.shoppingmall.bean.SkuLsParams;
import com.littlestore.shoppingmall.bean.SkuLsResult;
import com.littlestore.shoppingmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xxl
 * @create 2019-07-11 16:45
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private JestClient jestClient;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";

    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {

        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {
        /*
            1.  定义dsl 语句
            2.  创建执行的动作
            3.  执行动作
            4.  获取返回结果集
         */
        //制作Dsl语句
        String query = makeQueryStringForSearch(skuLsParams);

        //查询
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();

        //执行结果
        SearchResult searchResult = null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //制作返回结果集的方法
        SkuLsResult skuLsResult = makeResultForSearch(skuLsParams,searchResult);
        return skuLsResult;
    }

    private String makeQueryStringForSearch(SkuLsParams skuLsParams){
        //创建查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //创建查询query -- bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //设置keyword
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0){
            //设置检索条件match
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",skuLsParams.getKeyword());
            //给bool添加must - 添加match
            boolQueryBuilder.must(matchQueryBuilder);
            //设置高亮
            HighlightBuilder highlighter = searchSourceBuilder.highlight();
            //设置高亮字段，以及前缀后缀
            highlighter.field("skuName");
            highlighter.preTags("<sapn style='color:red'>");
            highlighter.postTags("</span>");
            // 将highlighter 放入高亮
            searchSourceBuilder.highlight(highlighter);
        }
        // 设置三级分类Id
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() >0){
            //设置term
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            // 给bool 添加filter -添加term
            boolQueryBuilder.filter(termQueryBuilder);
        }

        // 设置平台属性值Id
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length >0){
            //循环添加
            for (String valueId : skuLsParams.getValueId()) {
                TermQueryBuilder termQueryBuilder =
                        new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        //制作分页
        int from = skuLsParams.getPageSize()*(skuLsParams.getPageNo()-1);
        searchSourceBuilder.from(from);
        // 每页显示的大小
        searchSourceBuilder.size(skuLsParams.getPageSize());
        // 设置排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        // 设置聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);
        // 调用query查询 将bool 放入query 中
        searchSourceBuilder.query(boolQueryBuilder);
        String query = searchSourceBuilder.toString();

        System.out.println("query:"+query);

        return query;
    }


    /**
     * 制作返回结果集
     * @param skuLsParams
     * @param searchResult
     * @return
     */
    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult){
        //SkuLsResult
        SkuLsResult skuLsResult = new SkuLsResult();
        // List<SkuLsInfo> skuLsInfoList;
        // 声明一个集合来存储 dsl 语句查询之后的SkuLsInfo结果
        ArrayList<SkuLsInfo> skuLsInfoArrayList = new ArrayList<>();
        // 给skuLsInfoArrayList 集合赋值
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        if (hits != null && hits.size() >0){
            // 循环遍历取出skuLsInfo
            for (SearchResult.Hit<SkuLsInfo,Void> hit : hits){
                SkuLsInfo skuLsInfo = hit.source;
                // 获取高亮的SkuName
                if (hit.highlight != null && hit.highlight.size()>0){
                    // 获取高亮集合
                    List<String> list = hit.highlight.get("skuName");
                    String skuNameHI = list.get(0);
                    skuLsInfo.setSkuName(skuNameHI);
                }
                // 将es中的skuLsInfo 添加到集合
                skuLsInfoArrayList.add(skuLsInfo);
            }
        }
        // 将集合付给对象
        skuLsResult.setSkuLsInfoList(skuLsInfoArrayList);
        //long total
        skuLsResult.setTotal(searchResult.getTotal());
        //long totalPages
        long totalPages = (searchResult.getTotal()+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();
        skuLsResult.setTotalPages(totalPages);
        // 声明一个集合来存储平台属性值Id List<String> attrValueIdList;
        ArrayList<String> arrayList = new ArrayList<>();
        // 给arrayList 赋值
        // 获取平台属性值Id
        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();

        if (buckets != null && buckets.size() >0){
            for (TermsAggregation.Entry bucket : buckets) {
                String valueId = bucket.getKey();
                arrayList.add(valueId);
            }
        }
        skuLsResult.setAttrValueIdList(arrayList);
        return skuLsResult;
    }

}
