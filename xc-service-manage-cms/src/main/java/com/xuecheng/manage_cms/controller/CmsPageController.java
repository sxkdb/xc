package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QuestPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.course.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.CmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms")
public class CmsPageController implements CmsPageControllerApi {

    @Autowired
    private CmsPageService cmsPageService;

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(
            @PathVariable("page") int page,
            @PathVariable("size") int size, QuestPageRequest questPageRequest) {
        return cmsPageService.findList(page, size, questPageRequest);
    }

    /**
     * @param cmsPage 提交的数据是json格式的
     */
    @Override
    @PostMapping("/page/add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage) {
        return cmsPageService.add(cmsPage);
    }

    /**
     * @param pageId 该请求参数以键值对的形式传进来
     */
    @Override
    @GetMapping("/page/get/{id}")
    public CmsPageResult findById(@PathVariable("id") String pageId) {
        return cmsPageService.findById(pageId);
    }

    @Override
    @PutMapping("/page/edit/{pageId}")
    public CmsPageResult edit(@PathVariable("pageId") String pageId, @RequestBody CmsPage cmsPage) {
        return cmsPageService.update(pageId, cmsPage);
    }

    @Override
    @DeleteMapping("/page/delete/{pageId}")
    public ResponseResult delete(@PathVariable("pageId") String pageId) {
        return cmsPageService.delete(pageId);
    }

    @Override
    @PostMapping("/postPage/{pageId}")
    public ResponseResult postPage(@PathVariable("pageId") String pageId) {
        ResponseResult responseResult = cmsPageService.postPage(pageId);
        return responseResult;
    }

    @Override
    @PostMapping("/page/save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage) {
        return cmsPageService.save(cmsPage);
    }

    @Override
    @PostMapping("/page/postPageQuick")
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage) {
        return cmsPageService.postPageQuick(cmsPage);
    }


}
