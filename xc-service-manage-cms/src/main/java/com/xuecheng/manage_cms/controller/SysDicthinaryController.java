package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.SysDicthinaryControllerApi;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.service.SysDicthinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys")
public class SysDicthinaryController implements SysDicthinaryControllerApi {

    @Autowired
    private SysDicthinaryService sysDicthinaryService;

    @Override
    @GetMapping("/dictionary/get/{type}")
    public SysDictionary getByType(@PathVariable("type") String type) {
        return sysDicthinaryService.getByType(type);
    }
}
