package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsSiteControllerApi;
import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("cms")
public class CmsSiteController implements CmsSiteControllerApi {


    @Override
    @GetMapping("/site/list")
    public List<CmsSite> findList() {

        return null;
    }
}
