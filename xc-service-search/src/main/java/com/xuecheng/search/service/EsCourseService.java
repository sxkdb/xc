package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EsCourseService.class);

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Value("${xuecheng.elasticsearch.course.index}")
    private String index;

    @Value("${xuecheng.elasticsearch.course.type}")
    private String type;

    @Value("${xuecheng.elasticsearch.course.source_field}")
    private String[] source_field;


    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {

        if (page <= 0) page = 1;
        if (size <= 0) size = 10;

        if (courseSearchParam == null) {
            courseSearchParam = new CourseSearchParam();
        }

        //创建结果集
        QueryResult<CoursePub> queryResult = new QueryResult<>();
        List<CoursePub> coursePubs = new ArrayList<>();

        //创建查询对象 设置索引库和类型
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
        //创建搜索源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(source_field, new String[]{});

        //分页
        searchSourceBuilder.from((page - 1) * size);
        searchSourceBuilder.size(size);

        //高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //设置高亮的前后缀
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");
        //设置高亮的域
        highlightBuilder.field("name");
        searchSourceBuilder.highlighter(highlightBuilder);

        //创建布尔查询 用来组装多种查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //对关键字的查询
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
            MultiMatchQueryBuilder multiMatchQueryBuilder =
                    new MultiMatchQueryBuilder(courseSearchParam.getKeyword(), "name", "description", "teachplan")
                            .field("name", 10);//提升另个字段的Boost值
            //设置占比
            multiMatchQueryBuilder.minimumShouldMatch("70%");
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }


        //用过滤器性能比用查询好
        //一级分类的查询
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
        }

        //二级分类的查询
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
        }

        //难度等级
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }

        searchSourceBuilder.query(boolQueryBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {

            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("xuecheng search error..{}", e.getMessage());
            return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
        }

        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();

        queryResult.setTotal(totalHits);

        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            CoursePub coursePub = new CoursePub();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            //名称
            String name = (String) sourceAsMap.get("name");

            coursePub.setName(name);
            //图片
            String pic = (String) sourceAsMap.get("pic");
            coursePub.setPic(pic);
            //价格
            Double price = (Double) sourceAsMap.get("price");
            if (price != null) {
                coursePub.setPrice(price);
            }
            //旧的价格
            Double price_old = (Double) sourceAsMap.get("price_old");
            if (price_old != null) {
                coursePub.setPrice_old(price_old);
            }
            //取出id
            String id = (String) sourceAsMap.get("id");
            if (id != null) {
                coursePub.setId(id);
            }
            //取高亮字段
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            if (highlightFields != null) {
                HighlightField nameHighlightField = highlightFields.get("name");
                if (nameHighlightField != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    Text[] texts = nameHighlightField.getFragments();
                    for (Text text : texts) {
                        stringBuilder.append(text);
                    }
                    name = stringBuilder.toString();
                    coursePub.setName(name);
                }
            }

            if(StringUtils.isNotBlank(coursePub.getName())){
                coursePubs.add(coursePub);
            }
        }

        //将list放到结果集里面
        queryResult.setList(coursePubs);
        return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
    }


    public Map<String, CoursePub> getall(String id) {

        if (StringUtils.isBlank(id)) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }

        Map<String, CoursePub> resultMap = new HashMap<>();


        //根据课程id去elasticsearch中查询课程计划

        //创建查询对象 设置索引库和类型
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(type);
        //创建搜索源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(source_field, new String[]{});


        searchSourceBuilder.query(QueryBuilders.termsQuery("id", id));

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;
        try {

            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("xuecheng search error..{}", e.getMessage());
            return null;
        }

        SearchHits hits = searchResponse.getHits();

        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            CoursePub coursePub = new CoursePub();
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            //名称
            String name = (String) sourceAsMap.get("name");

            coursePub.setName(name);
            //图片
            String pic = (String) sourceAsMap.get("pic");
            coursePub.setPic(pic);
            //价格
            Double price = (Double) sourceAsMap.get("price");
            if (price != null) {
                coursePub.setPrice(price);
            }
            //旧的价格
            Double price_old = (Double) sourceAsMap.get("price_old");
            if (price_old != null) {
                coursePub.setPrice_old(price_old);
            }
            //取出id
            String coursePubId = (String) sourceAsMap.get("id");
            if (coursePubId != null) {
                coursePub.setId(coursePubId);
            }
            String users = (String) sourceAsMap.get("users");
            coursePub.setUsers(users);
            String grade = (String) sourceAsMap.get("grade");
            coursePub.setGrade(grade);
            String teachmode = (String) sourceAsMap.get("teachmode");
            coursePub.setTeachplan(teachmode);
            String description = (String) sourceAsMap.get("description");
            coursePub.setDescription(description);
            String charge = (String) sourceAsMap.get("charge");
            coursePub.setCharge(charge);
            String valid = (String) sourceAsMap.get("valid");
            coursePub.setValid(valid);
            String expires = (String) sourceAsMap.get("expires");
            coursePub.setExpires(expires);
            String pubTime = (String) sourceAsMap.get("pubTime");
            coursePub.setPubTime(pubTime);
            String teachplan = (String) sourceAsMap.get("teachplan");
            coursePub.setTeachplan(teachplan);
            String qq = (String) sourceAsMap.get("qq");
            coursePub.setQq(qq);
            resultMap.put(coursePubId, coursePub);
        }

        return resultMap;
    }

    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds) {


        //创建查询对象 设置索引库和类型
        SearchRequest searchRequest = new SearchRequest("zsh_course_media");
        searchRequest.types(type);
        //创建搜索源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"courseid", "teachplan_id", "media_id", "media_url", "media_fileoriginalname"}, new String[]{});

        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id", teachplanIds));

        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;
        try {

            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("xuecheng search error..{}", e.getMessage());
            return null;
        }

        SearchHits hits = searchResponse.getHits();
        long total = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();


        //数据列表
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();

        for (SearchHit searchHit : searchHits) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();

            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String courseid = (String) sourceAsMap.get("courseid");
            String teachplan_id = (String) sourceAsMap.get("teachplan_id");
            String media_url = (String) sourceAsMap.get("media_url");
            String media_id = (String) sourceAsMap.get("media_id");
            String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");

            teachplanMediaPub.setCourseId(courseid);
            teachplanMediaPub.setTeachplanId(teachplan_id);
            teachplanMediaPub.setMediaUrl(media_url);
            teachplanMediaPub.setMediaId(media_id);
            teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);

            teachplanMediaPubList.add(teachplanMediaPub);
        }


        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubList);
        queryResult.setTotal(total);

        return new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
    }
}
