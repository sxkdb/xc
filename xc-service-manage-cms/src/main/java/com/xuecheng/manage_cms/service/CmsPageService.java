package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QuestPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRespository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRespository;
import com.xuecheng.manage_cms.dao.CmsTemplateRespository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 页面管理service
 */
@Service
public class CmsPageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private CmsConfigRespository cmsConfigRespository;

    @Autowired
    private CmsTemplateRespository cmsTemplateRespository;

    @Autowired
    private CmsSiteRespository cmsSiteRespository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 分页查询+条件查询 cmsPage
     */
    public QueryResponseResult findList(int page, int size, QuestPageRequest questPageRequest) {

        //提交查询逻辑
        if (questPageRequest == null) {
            questPageRequest = new QuestPageRequest();
        }
        //条件匹配器 对pageAliase和pageName实现模糊查询
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains())
                .withMatcher("pageName", ExampleMatcher.GenericPropertyMatchers.contains());
        CmsPage cmsPage = new CmsPage();

        //判断是否为空
        //设置站点id
        if (StringUtils.isNotEmpty(questPageRequest.getSiteId())) {
            cmsPage.setSiteId(questPageRequest.getSiteId());
        }
        //设置模板id
        if (StringUtils.isNotEmpty(questPageRequest.getTemplateId())) {
            cmsPage.setTemplateId(questPageRequest.getTemplateId());
        }
        //设置页面别名
        if (StringUtils.isNotEmpty(questPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(questPageRequest.getPageAliase());
        }
        //设置页面名字
        if (StringUtils.isNotBlank(questPageRequest.getPageName())) {
            cmsPage.setPageName(questPageRequest.getPageName());
        }
        //设置页面类型
        if (StringUtils.isNotEmpty(questPageRequest.getPageType())) {
            cmsPage.setPageType(questPageRequest.getPageType());
        }

        Example<CmsPage> example = Example.of(cmsPage, matcher);

        //分页逻辑
        if (page <= 0) {
            page = 1;
        }
        page--;
        if (size <= 0) {
            size = 10;
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<CmsPage> pages = cmsPageRepository.findAll(example, pageable);

        QueryResult queryResult = new QueryResult();
        queryResult.setList(pages.getContent());
        queryResult.setTotal(pages.getTotalElements());

        ResultCode resultCode = CommonCode.SUCCESS;
        QueryResponseResult result = new QueryResponseResult(resultCode, queryResult);

        return result;

    }

    /**
     * 添加cmsPage
     */
    public CmsPageResult add(CmsPage cmsPage) {

        if (cmsPage == null /*|| cmsPage.getSiteId() == null*/) {
            ExceptionCast.cast(CmsCode.CMS_ILLEGALPARAMETER);
        }

        //添加cmsPage前先查询数据是否重复
        CmsPage page = cmsPageRepository.findBySiteIdAndPageNameAndPageWebPath(cmsPage.getSiteId(), cmsPage.getPageName(), cmsPage.getPageWebPath());
        if (page == null) {
            //可以执行插入
            //在插入前将主键置位null  防止有人对主键也提交了数据   主键要交由mongodb自己生成
            cmsPage.setPageId(null);
            cmsPageRepository.save(cmsPage);
            //返回正确信息
            return new CmsPageResult(CommonCode.SUCCESS, cmsPage);
        }
        //数据重复，返回错误信息
        return new CmsPageResult(CommonCode.FAIL, null);

    }

    /**
     * 根据id查询cmsPage
     */
    public CmsPageResult findById(String pageId) {
        Optional<CmsPage> optionalCmsPage = cmsPageRepository.findById(pageId);
        if (optionalCmsPage.isPresent()) {
            return new CmsPageResult(CommonCode.SUCCESS, optionalCmsPage.get());
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
     * 修改cmsPage
     */
    public CmsPageResult update(String pageId, CmsPage cmsPage) {
        //根据id从数据库中查出对应的数据，再一个一个set
        CmsPageResult find = findById(pageId);
        CmsPage page = find.getCmsPage();
        if (page != null) {
            //更新模板id
            page.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            page.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            page.setPageAliase(cmsPage.getPageAliase());
            // 更新页面名称
            page.setPageName(cmsPage.getPageName());
            // 更新访问路径
            page.setPageWebPath(cmsPage.getPageWebPath());
            // 更新物理路径
            page.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新数据url
            page.setDataUrl(cmsPage.getDataUrl());
            cmsPageRepository.save(page);
            return new CmsPageResult(CommonCode.SUCCESS, page);
        }
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
     * 删除cmsPage
     */
    public ResponseResult delete(String pageId) {
        boolean exists = cmsPageRepository.existsById(pageId);
        if (exists) {
            cmsPageRepository.deleteById(pageId);
            return new ResponseResult();
        }
        return new ResponseResult(CommonCode.FAIL);
    }


    /**
     * 根据id查询CmsConfig
     * cmsPage中的dataUrl可以请求到这里
     */
    public CmsConfig getMedol(String id) {
        Optional<CmsConfig> optional = cmsConfigRespository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    /**
     * 页面静态化
     * 步骤：
     * 1.获取模型数据
     * 1).根据pageId去cms_page查找对应的cmsPage,拿到里面的dataUrl
     * 2).拿着dataUrl，使用restTemplate远程访问cms_config获取对应的模型数据
     * 2.获取模板
     * 1).根据pageId查找对应的cmsPage,拿到里面的templateId
     * 2).根据templateId去cms_template查找对应的CmsTemplate,获取里面的templateFileId
     * 3).根据templateFileId去cms_files查找对应的模板内容
     * 3.数据模型 + 模板 执行静态化
     *
     * @return 返回 模型数据+页面模板 的文本字符串
     */
    public String getHtml(String pageId) {
        //获取模型数据
        Map model = getCmsConfigModel(pageId);
        //获取页面模板
        String template = getTemplateByPageId(pageId);
        if (template == null) {
            ExceptionCast.cast(CmsCode.CMS_FILE_NOTEXISTS);
        }
        //执行静态化
        String content = performStatic(model, template);
        if (StringUtils.isEmpty(content)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
        }
        return content;
    }


    //获取模型数据
    private Map getCmsConfigModel(String pageId) {
        CmsPageResult cmsPageResult = findById(pageId);
        CmsPage cmsPage = cmsPageResult.getCmsPage();
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        Map map = restTemplate.getForObject(dataUrl, Map.class);
        if (map == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        return map;
    }

    //获取页面模板
    private String getTemplateByPageId(String pageId) {
        //根据pageId获取cmsPage的templateId
        CmsPageResult cmsPageResult = findById(pageId);
        CmsPage cmsPage = cmsPageResult.getCmsPage();
        if (cmsPage == null) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)) {
            ExceptionCast.cast(CmsCode.CMS_PAGETEMPLATEId_NOTEXISTS);
        }
        //根据templateId查询CmsTemplate中对应的templateFileId
        Optional<CmsTemplate> optional = cmsTemplateRespository.findById(templateId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        CmsTemplate cmsTemplate = optional.get();
        String templateFileId = cmsTemplate.getTemplateFileId();
        if (StringUtils.isEmpty(templateFileId)) {
            ExceptionCast.cast(CmsCode.CMS_PAGETEMPLATEFILEId_NOTEXISTS);
        }
        //根据templateFileId查到对应的模板文件内容
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
        //打开下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        try {
            String content = IOUtils.toString(gridFsResource.getInputStream(), "UTF-8");
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //执行静态化
    private String performStatic(Map model, String templateContent) {
        //创建配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateContent);
        //配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        try {
            //获取模板
            Template template = configuration.getTemplate("template");
            //生成静态数据
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return html;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将静态页面到mongodb中
     */
    public ResponseResult postPage(String pageId) {
        //执行静态化
        String htmlContent = this.getHtml(pageId);
        //保存静态文件
        CmsPage cmsPage = this.saveHtml(pageId, htmlContent);
        //发送消息
        this.sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 发送保存页面的消息
     */
    public void sendPostPage(String pageId) {

        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();
        Map map = new HashMap();
        map.put("pageId", pageId);
        String json = JSON.toJSONString(map);
        //指定了RoutingKey为siteid
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, cmsPage.getSiteId(), json);

    }

    public CmsPage saveHtml(String pageId, String htmlContent) {
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        CmsPage cmsPage = optional.get();
        String htmlFileId = cmsPage.getHtmlFileId();
        //保存之前先删除
        if (StringUtils.isNotEmpty(htmlFileId)) {
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
        //保存html文件
        try {
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "UTF-8");
            ObjectId id = gridFsTemplate.store(inputStream, cmsPage.getPageName());
            //文件id
            String fileId = id.toString();
            //更新文件id
            cmsPage.setHtmlFileId(fileId);
            cmsPageRepository.save(cmsPage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cmsPage;

    }


    //添加页面 如有有执行更新 如果没有执行添加
    public CmsPageResult save(CmsPage cmsPage) {
        if (StringUtils.isNotBlank(cmsPage.getSiteId()) && StringUtils.isNotBlank(cmsPage.getPageName()) && StringUtils.isNotBlank(cmsPage.getPageWebPath())) {
            CmsPage page = cmsPageRepository.findBySiteIdAndPageNameAndPageWebPath(cmsPage.getSiteId(), cmsPage.getPageName(), cmsPage.getPageWebPath());
            if (page == null) {
                //执行添加
                return this.add(cmsPage);
            }
            //执行更新
            return this.update(page.getPageId(), cmsPage);
        }
        return new CmsPageResult(CmsCode.CMS_ILLEGALPARAMETER, null);
    }

    //页面一键发布  供course服务调用
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {

        //保存或更新cmspage
        CmsPageResult save = this.save(cmsPage);
        if (!save.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        CmsPage saveCmsPage = save.getCmsPage();
        String siteId = saveCmsPage.getSiteId();
        CmsSite cmsSite = findCmsSiteById(siteId);
        if (cmsSite == null) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //拼接url
        String pageUrl = cmsSite.getSiteDomain() + cmsSite.getSiteWebPath() + saveCmsPage.getPageWebPath() + saveCmsPage.getPageName();
        //页面发布
        ResponseResult responseResult = this.postPage(saveCmsPage.getPageId());
        if (!responseResult.isSuccess()) {
            return new CmsPostPageResult(CommonCode.FAIL, null);
        }
        return new CmsPostPageResult(CommonCode.SUCCESS, pageUrl);
    }
    //查询站点
    public CmsSite findCmsSiteById(String cmsSiteId) {
        Optional<CmsSite> optional = cmsSiteRespository.findById(cmsSiteId);
        return optional.orElse(null);
    }
}
