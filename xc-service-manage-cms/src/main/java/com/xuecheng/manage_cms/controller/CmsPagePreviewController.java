package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.CmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;

/**
 * 页面预览
 */
@Controller
public class CmsPagePreviewController extends BaseController {

    @Autowired
    private CmsPageService cmsPageService;

    @GetMapping(value = "/cms/preview/{pageId}")
    @ResponseBody
    public String preview(@PathVariable("pageId") String pageId){
        String html = cmsPageService.getHtml(pageId);
        return html;
    }


    //TODO 这个方法不知道为什么设置不了contentType
    @GetMapping(value = "/cms/preview2/{pageId}")
    public void preview2(@PathVariable("pageId") String pageId){
        String html = cmsPageService.getHtml(pageId);
        try {
            response.getOutputStream().write(html.getBytes(StandardCharsets.UTF_8));
            //response.setHeader("Content-type","text/html;charset=utf-8");
            response.setContentType("text/html;charset=UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*@GetMapping("/cms/preview/{pageId}")
    public void preview(@PathVariable("pageId") String pageId, HttpServletResponse response){
        String html = cmsPageService.getHtml(pageId);
        try {
            response.getWriter().write(html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

}
