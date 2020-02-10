package com.xuecheng.search.test;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {


    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private RestClient restClient;

    //查询全部
    @Test
    public void testSearchAll() throws Exception {

        // 创建查询对象  指定索引库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        // 设置查询类型
        searchRequest.types("doc");
        // 查询源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置查询源  查询全部
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        // source源字段过滤   第一个参数是要查询出来的字段  第二个参数是不要查询出来的字段
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        // 查询
        SearchResponse searchResponse = client.search(searchRequest);
        // 执行搜索的分片总数
        int totalShards = searchResponse.getTotalShards();

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        // 与搜索请求匹配的匹配总数。
        long hitsTotalHits = hits.getTotalHits();

        for (SearchHit searchHit : searchHits) {
            String index = searchHit.getIndex();
            String type = searchHit.getType();
            String id = searchHit.getId();
            float score = searchHit.getScore();
            String sourceAsString = searchHit.getSourceAsString();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            System.out.println(name);
            System.out.println(studymodel);
        }

    }

    //分页查询
    @Test
    public void testSearchPage() throws Exception {

        //创建查询对象 指定索引库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置分页
        int page = 1;
        int size = 1;
        searchSourceBuilder.from((page - 1) * size);
        searchSourceBuilder.size(size);
        //source过滤字段
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);

        // 执行搜索的分片总数
        int totalShards = searchResponse.getTotalShards();
                
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        // 与搜索请求匹配的匹配总数。
        long hitsTotalHits = hits.getTotalHits();

        for (SearchHit searchHit : searchHits) {
            String index = searchHit.getIndex();
            String type = searchHit.getType();
            String id = searchHit.getId();
            float score = searchHit.getScore();
            String sourceAsString = searchHit.getSourceAsString();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            System.out.println(name);
            System.out.println(studymodel);
        }


    }


    //精确查询
    @Test
    public void testTermQuery() throws Exception {


        //创建查询对象 指定索引库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring" ));

        //source过滤字段
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);


        // 执行搜索的分片总数
        int totalShards = searchResponse.getTotalShards();

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        // 与搜索请求匹配的匹配总数。
        long hitsTotalHits = hits.getTotalHits();

        for (SearchHit searchHit : searchHits) {
            String index = searchHit.getIndex();
            String type = searchHit.getType();
            String id = searchHit.getId();
            //匹配度
            float score = searchHit.getScore();
            String sourceAsString = searchHit.getSourceAsString();
            //将文档转成map
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            System.out.println(name);
            System.out.println(studymodel);
        }
    }


    //根据id精确查询
    @Test
    public void testTermQueryByid() throws Exception {


        //创建查询对象 指定索引库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //注意这里和之前的不一样 是termsQuery 加了s
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id", "1","2"));

        //source过滤字段
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel"}, new String[]{});
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);


        // 执行搜索的分片总数
        int totalShards = searchResponse.getTotalShards();

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        // 与搜索请求匹配的匹配总数。
        long hitsTotalHits = hits.getTotalHits();

        for (SearchHit searchHit : searchHits) {
            String index = searchHit.getIndex();
            String type = searchHit.getType();
            String id = searchHit.getId();
            //匹配度
            float score = searchHit.getScore();
            String sourceAsString = searchHit.getSourceAsString();
            //将文档转成map
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            System.out.println(name);
            System.out.println(studymodel);
        }
    }


    //全文检索   搜索的时候分词
    @Test
    public void testMatchQuery() throws Exception {
        //创建查询对象 指定索引库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel","description"}, new String[]{});
        //匹配关键子
        //operator：or 表示 只要有一个词在文档中出现则就符合条件，and表示每个词都在文档中出现则才符合条件
//        searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开发").operator(Operator.OR));
        //设置匹配占比
        searchSourceBuilder.query(QueryBuilders.matchQuery("description", "spring开发").minimumShouldMatch("80%"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }


    }

    //multi Query  一次可以匹配多个字段
    @Test
    public void testMultiQuery() throws Exception {


        //创建查询对象 指定索引库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel","description"}, new String[]{});

        MultiMatchQueryBuilder multiMatchQueryBuilder = new MultiMatchQueryBuilder("spring框架", "name","description").minimumShouldMatch("50%");
        //提升boost
        multiMatchQueryBuilder.field("name",10);
        searchSourceBuilder.query(multiMatchQueryBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }


    }



    //BooleanQuery 多个查询组合起开
    @Test
    public void testBooleanQuery() throws Exception {


        //创建查询对象 指定索引库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel","description"}, new String[]{});

        MultiMatchQueryBuilder multiMatchQueryBuilder = new MultiMatchQueryBuilder("spring框架", "name","description").minimumShouldMatch("50%");
        //提升boost
        multiMatchQueryBuilder.field("name",10);
        searchSourceBuilder.query(multiMatchQueryBuilder);

        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("studymodel", "201001");
        //创建布尔查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //添加两种查询类型
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
        }


    }

    //过滤器
    @Test
    public void testFilter() throws Exception {

        //创建查询对象 指定索引库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "description","price"}, new String[]{});

        MultiMatchQueryBuilder multiMatchQueryBuilder = new MultiMatchQueryBuilder("spring框架", "name", "description").minimumShouldMatch("50%");
        //提升boost
        multiMatchQueryBuilder.field("name", 10);
        searchSourceBuilder.query(multiMatchQueryBuilder);

        //创建布尔查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //添加两种查询类型
        boolQueryBuilder.must(multiMatchQueryBuilder);
        //过滤器
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));

        searchSourceBuilder.query(boolQueryBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            Object price = sourceAsMap.get("price");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
            System.out.println(price);
        }
    }
    //排序
    @Test
    public void testSort() throws Exception {

        //创建查询对象 指定索引库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "description","price"}, new String[]{});

        MultiMatchQueryBuilder multiMatchQueryBuilder = new MultiMatchQueryBuilder("spring框架", "name", "description").minimumShouldMatch("50%");
        //提升boost
        multiMatchQueryBuilder.field("name", 10);
        searchSourceBuilder.query(multiMatchQueryBuilder);

        //创建布尔查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //添加两种查询类型
        boolQueryBuilder.must(multiMatchQueryBuilder);
        //过滤器
//        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", "201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));

        //排序
        searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.ASC));//升序
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.DESC));//降序

        searchSourceBuilder.query(boolQueryBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest);

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            Object price = sourceAsMap.get("price");
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(description);
            System.out.println(price);
        }
    }

    //高亮
    @Test
    public void testHighlight() throws Exception {

        //创建查询对象 指定索引库
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "description","price"}, new String[]{});
        //搜索词汇 匹配的域 占比
        MultiMatchQueryBuilder multiMatchQueryBuilder = new MultiMatchQueryBuilder("开发", "name", "description").minimumShouldMatch("50%");
        //提升boost
        multiMatchQueryBuilder.field("name", 10);
        searchSourceBuilder.query(multiMatchQueryBuilder);
        //创建布尔查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //添加查询类型
        boolQueryBuilder.must(multiMatchQueryBuilder);
        //过滤器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));
        //排序
        searchSourceBuilder.sort(new FieldSortBuilder("studymodel").order(SortOrder.ASC));
        searchSourceBuilder.sort(new FieldSortBuilder("price").order(SortOrder.DESC));
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<p>");
        highlightBuilder.postTags("</p>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        highlightBuilder.fields().add(new HighlightBuilder.Field("description"));

        searchSourceBuilder.highlighter(highlightBuilder);

        searchSourceBuilder.query(boolQueryBuilder);

        searchRequest.source(searchSourceBuilder);
        //执行查询
        SearchResponse searchResponse = client.search(searchRequest);

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String studymodel = (String) sourceAsMap.get("studymodel");
            String description = (String) sourceAsMap.get("description");
            Object price = sourceAsMap.get("price");

            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();

            if (highlightFields != null){

                HighlightField namehighlightField = highlightFields.get("name");

                name = this.getString(name, namehighlightField);

                HighlightField descriptionHighlightField = highlightFields.get("description");
                description = this.getString(description, descriptionHighlightField);

            }

            System.out.println("name="+name);
            System.out.println("studymodel="+studymodel);
            System.out.println("description="+description);
            System.out.println("price="+price);
        }
    }

    private String getString(String name, HighlightField namehighlightField) {
        if (namehighlightField != null) {
            StringBuilder stringBuilder  = new StringBuilder();
            Text[] fragments = namehighlightField.getFragments();
            for (Text fragment : fragments) {
                stringBuilder.append(fragment.string());
            }
            name = stringBuilder.toString();
        }
        return name;
    }


}
