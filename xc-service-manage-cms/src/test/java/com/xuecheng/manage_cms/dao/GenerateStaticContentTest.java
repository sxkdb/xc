package com.xuecheng.manage_cms.dao;

import com.xuecheng.manage_cms.service.CmsPageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GenerateStaticContentTest {

    @Autowired
    private CmsPageService cmsPageService;

    @Test
    public void test() {

        String html = cmsPageService.getHtml("5a795ac7dd573c04508f3a56");
        System.out.println(html);

    }


}
