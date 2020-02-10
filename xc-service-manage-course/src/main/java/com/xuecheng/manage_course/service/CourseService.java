package com.xuecheng.manage_course.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.netflix.discovery.converters.Auto;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CoursePublishResult;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.Courseview;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmspageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourseService.class);

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanRespository teachplanRespository;

    @Autowired
    private CourseBaseRepository courseBaseRepository;

    @Autowired
    private CourseMarketRepository courseMarketRepository;

    @Autowired
    private CoursePicRepository coursePicRepository;

    @Autowired
    private CmspageClient cmspageClient;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CoursePubRepository coursePubRepository;

    @Autowired
    private TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    private TeachplanMediaPubRepository teachplanMediaPubRepository;

    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;

    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;

    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;

    @Value("${course-publish.siteId}")
    private String publish_siteId;

    @Value("${course-publish.templateId}")
    private String publish_templateId;

    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    public TeachplanNode findTeachplanList(String courseId) {
        TeachplanNode teachplanList = null;
        if (StringUtils.isNotEmpty(courseId)) {
            teachplanList = teachplanMapper.findTeachplanList(courseId);
        }
        return teachplanList;
    }

    /**
     * 添加课程计划
     * 步骤
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {

        if (teachplan == null
                || StringUtils.isBlank(teachplan.getPname())
                || StringUtils.isBlank(teachplan.getCourseid())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程的id
        String courseid = teachplan.getCourseid();
        //获取课程根节点
        String parentid = teachplan.getParentid();
        //要处理根节点parentId
        //如果parentId为空 手动根据courseId去teachplan中获取根节点  如果没有 则拿着courseId去courseBase中查询获取对应的根节点信息，添加根节点到teachplan
        if (StringUtils.isBlank(parentid)) {
            parentid = getTeachplanRoot(courseid);

            if (StringUtils.isBlank(parentid)) {
                ExceptionCast.cast(CommonCode.INVALID_PARAM);
            }
            //将parentId设置到teachplan中
            teachplan.setParentid(parentid);
        }
        //设置其他属性
        //设置级别grade 根据父节点的grade来设置
        Optional<Teachplan> optional = teachplanRespository.findById(teachplan.getParentid());
        Teachplan parentNode = optional.get();
        String grade = parentNode.getGrade();
        if (grade.equals("1")) {
            teachplan.setGrade("2");
        } else if (grade.equals("2")) {
            teachplan.setGrade("3");
        }
        //执行保存
        teachplanRespository.save(teachplan);
        //返回成功消息
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //获取根节点 如果没有 就添加
    public String getTeachplanRoot(String courseId) {
        //根据courseId的值和parent=0来获取对应的teachplan
        List<Teachplan> teachplans = teachplanRespository.getTeachplansByCourseidAndParentid(courseId, "0");
        //如果没有teachplan  则拿着courseId去courseBase中找对应的数据
        if (teachplans == null || teachplans.size() <= 0) {
            Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
            //如果存在对应的courseBase
            if (optional.isPresent()) {
                //添加一个新的teachplan到teachplan中，并返回对应的id
                CourseBase courseBase = optional.get();
                Teachplan teachplanNew = new Teachplan();
                teachplanNew.setCourseid(courseBase.getId()); //课程id
                teachplanNew.setParentid("0");//父级节点
                teachplanNew.setStatus("0");  //0--未发布  1--发布
                teachplanNew.setGrade("1");   //层级 1、2、3级
                teachplanNew.setPname(courseBase.getName()); //课程名称
                teachplanRespository.save(teachplanNew);
                return teachplanNew.getId();
            }
            //如果找不到对应的courseBase,返回空
            return null;
        }
        //如果有teachplan 直接返回对应的id
        Teachplan teachplan = teachplans.get(0);
        return teachplan.getId();
    }

    /**
     * 查询课程列表
     */
    public QueryResponseResult findCourseList(int page, int size, CourseListRequest courseListRequest) {
        if (page <= 0) {
            page = 1;
        }
        if (size <= 0) {
            size = 6;
        }
        //添加分页条件
        PageHelper.startPage(page, size);
        //执行查询
        Page<CourseInfo> courseInfos = courseMapper.findCourseInfos();

        long total = courseInfos.getTotal();
        List<CourseInfo> result = courseInfos.getResult();
        QueryResult queryResult = new QueryResult();
        queryResult.setTotal(total);
        queryResult.setList(result);

        return new QueryResponseResult(CommonCode.SUCCESS, queryResult);
    }


    @Transactional
    public ResponseResult addCourseBase(CourseBase courseBase) {
        if (courseBase == null || StringUtils.isBlank(courseBase.getName())
                || StringUtils.isBlank(courseBase.getUsers())) {
            return new ResponseResult(CommonCode.INVALID_PARAM);
        }
        courseBaseRepository.save(courseBase);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CourseBase getCourseBaseById(String courseId) {
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            LOGGER.error("根据courseId查不到对应的课程基本信息");
            return null;
        }
        return optional.get();
    }

    @Transactional
    public ResponseResult updateCourseBase(String id, CourseBase courseBase) {

        //查出课程的基本信息
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        CourseBase base = optional.get();
        //然后一个一个值设置进去
        //设置名字
        if (StringUtils.isNotBlank(courseBase.getName())) {
            base.setName(courseBase.getName());
        }
        //设置适用人群
        if (StringUtils.isNotBlank(courseBase.getUsers())) {
            base.setUsers(courseBase.getUsers());
        }
        //设置等级
        if (StringUtils.isNotBlank(courseBase.getGrade())) {
            base.setGrade(courseBase.getGrade());
        }
        //设置课程大分类
        if (StringUtils.isNotBlank(courseBase.getMt())) {
            base.setMt(courseBase.getMt());
        }
        //设置课程小分类
        if (StringUtils.isNotBlank(courseBase.getSt())) {
            base.setSt(courseBase.getSt());
        }
        //设置课程介绍
        if (StringUtils.isNotBlank(courseBase.getDescription())) {
            base.setDescription(courseBase.getDescription());
        }
        //保存数据
        courseBaseRepository.save(base);
        //返回成功
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CourseMarket getCourseMarketById(String courseId) {

        Optional<CourseMarket> optional = courseMarketRepository.findById(courseId);
        return optional.orElse(null);
    }

    @Transactional
    public ResponseResult updateCourseMarket(String id, CourseMarket courseMarket) {

        CourseMarket market = this.getCourseMarketById(id);
        //如果为空
        if (market == null) {
            //执行添加保存
            courseMarketRepository.save(courseMarket);
            return new ResponseResult(CommonCode.SUCCESS);
        }

        if (courseMarket.getPrice() != null && courseMarket.getPrice() >= 0) {
            market.setPrice(courseMarket.getPrice());
        }
        if (courseMarket.getStartTime() != null) {
            market.setStartTime(courseMarket.getStartTime());
        }
        if (courseMarket.getEndTime() != null) {
            market.setEndTime(courseMarket.getEndTime());
        }
        if (StringUtils.isNotEmpty(courseMarket.getQq())) {
            market.setQq(courseMarket.getQq());
        }
        if (StringUtils.isNotEmpty(courseMarket.getCharge())) {
            market.setCharge(courseMarket.getCharge());
        }
        if (StringUtils.isNotEmpty(courseMarket.getValid())) {
            market.setValid(courseMarket.getValid());
        }
        if (courseMarket.getPrice_old() != null && courseMarket.getPrice_old() >= 0) {
            market.setPrice_old(courseMarket.getPrice_old());
        }

        courseMarketRepository.save(market);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (optional.isPresent()) {
            coursePic = optional.get();
            coursePic.setPic(pic);
        } else {
            coursePic = new CoursePic();
            coursePic.setCourseid(courseId);
            coursePic.setPic(pic);
        }
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    public CoursePic findCoursepic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        return optional.orElse(null);
    }

    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        //删除数据库中的信息
        int count = coursePicRepository.deleteByCourseid(courseId);
        if (count > 0) {
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //查询课程视图所需要的信息
    public Courseview getCourseview(String id) {

        Courseview courseview = new Courseview();

        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        courseBaseOptional.ifPresent(courseview::setCourseBase);

        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        coursePicOptional.ifPresent(courseview::setCoursePic);

        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        courseMarketOptional.ifPresent(courseview::setCourseMarket);

        TeachplanNode teachplanList = this.findTeachplanList(id);
        courseview.setTeachplanNode(teachplanList);

        return courseview;
    }

    //课程预览
    public CoursePublishResult preview(String courseId) {

        CourseBase courseBase = this.findCourseBaseById(courseId);
        if (courseBase == null) {
            return new CoursePublishResult(CourseCode.COURSE_NOTEXIST_COURSEBASE, null);
        }
        //发布课程预览页面
        CmsPage cmspage = new CmsPage();
        cmspage.setPageAliase(courseBase.getName());
        cmspage.setPageName(courseId + ".html");
        cmspage.setPageWebPath(publish_page_webpath);
        cmspage.setPagePhysicalPath(publish_page_physicalpath);
        cmspage.setTemplateId(publish_templateId);
        cmspage.setSiteId(publish_siteId);
        cmspage.setDataUrl(publish_dataUrlPre + courseId);
        //远程请求cms保存页面信息  执行的是保存或者更加
        CmsPageResult cmsPageResult = cmspageClient.save(cmspage);
        if (!cmsPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        String pageUrl = previewUrl + cmsPageResult.getCmsPage().getPageId();

        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    //根据页面id查询页面基本信息
    private CourseBase findCourseBaseById(String courseId) {
        return courseBaseRepository.findById(courseId).orElse(null);
    }

    //课程发布
    @Transactional
    public CoursePublishResult publish(String courseId) {
        CourseBase courseBase = this.findCourseBaseById(courseId);
        if (courseBase == null) {
            return new CoursePublishResult(CourseCode.COURSE_NOTEXIST_COURSEBASE, null);
        }
        //发布课程预览页面
        CmsPage cmspage = new CmsPage();
        cmspage.setPageAliase(courseBase.getName());
        cmspage.setPageName(courseId + ".html");
        cmspage.setPageWebPath(publish_page_webpath);
        cmspage.setPagePhysicalPath(publish_page_physicalpath);
        cmspage.setTemplateId(publish_templateId);
        cmspage.setSiteId(publish_siteId);
        cmspage.setDataUrl(publish_dataUrlPre + courseId);
        //调用cms服务  页面一键发布
        CmsPostPageResult cmsPostPageResult = cmspageClient.postPageQuick(cmspage);
        if (!cmsPostPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        /*
            修改课程状态
              制作中 202001
              已发布 202002
              已下线 202003
         */
        courseBase.setStatus("202002");
        courseBaseRepository.save(courseBase);

        //保存课程索引信息
        //创建coursePub
        CoursePub coursePub = this.createCoursePub(courseId);
        //保存coursePub
        this.saveCoursePub(courseId, coursePub);

        //将发布信息保存到teachplanMediaPub表中

        //先删除teachplanMediaPub
        teachplanMediaPubRepository.deleteByCourseId("2");

        List<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        for (TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub );
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubs.add(teachplanMediaPub);
        }

        //再插入数据到teachplanMediaPub
        teachplanMediaPubRepository.saveAll(teachplanMediaPubs);

        //发布成功
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }


    //保存coursePub
    private CoursePub saveCoursePub(String id, CoursePub coursePub) {

        CoursePub coursePubNew = null;

        //根据id查coursePub
        Optional<CoursePub> optional = coursePubRepository.findById(id);

        //存在就返回，否者就new一个
        coursePubNew = optional.orElseGet(CoursePub::new);

        BeanUtils.copyProperties(coursePub, coursePubNew);
        //设置主键
        coursePubNew.setId(id);
        //设置时间戳
        coursePubNew.setTimestamp(new Date());
        //设置发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY‐MM‐dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(format);

        //执行保存
        coursePubRepository.save(coursePubNew);

        return coursePubNew;
    }

    //创建coursePub
    private CoursePub createCoursePub(String id) {
        CoursePub coursePub = new CoursePub();

        if (StringUtils.isNotBlank(id)) {
            //基本信息
            Optional<CourseBase> optional = courseBaseRepository.findById(id);
            if (optional.isPresent()) {
                CourseBase courseBase = optional.get();
                BeanUtils.copyProperties(courseBase, coursePub);
            }
            //查询课程图片
            Optional<CoursePic> picOptional = coursePicRepository.findById(id);
            if (picOptional.isPresent()) {
                CoursePic coursePic = picOptional.get();
                BeanUtils.copyProperties(coursePic, coursePub);
            }
            //课程营销信息
            Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
            if (marketOptional.isPresent()) {
                CourseMarket courseMarket = marketOptional.get();
                BeanUtils.copyProperties(courseMarket, coursePub);
            }
            //课程计划
            TeachplanNode teachplanList = teachplanMapper.findTeachplanList(id);
            String string = JSON.toJSONString(teachplanList);
            coursePub.setTeachplan(string);

        }

        return coursePub;
    }

    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {

        //检查teachplanMedia
        if (teachplanMedia == null) {
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }
        String teachplanId = teachplanMedia.getTeachplanId();

        if (StringUtils.isBlank(teachplanId)) {
            ExceptionCast.cast(CommonCode.INVALIDPARAM);
        }

        //查询对应绑定的课程计划的层数是否死最低层 - 3
        Optional<Teachplan> optional = teachplanRespository.findById(teachplanId);

        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }

        Teachplan teachplan = optional.get();

        String grade = teachplan.getGrade();
        if (StringUtils.isBlank(grade) || !grade.equals("3")) {
            ExceptionCast.cast(CommonCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        //保存或更新teachplanMedia
        teachplanMediaRepository.save(teachplanMedia);

        return new ResponseResult(CommonCode.SUCCESS);
    }
}
