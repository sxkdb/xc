package com.xuecheng.manage_course;

import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.manage_course.client.CmspageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestFeign {

    @Autowired
    CmspageClient cmsPageClient;

    @Test
    public void testFeign() {
        CmsPageResult result = cmsPageClient.findById("5a795ac7dd573c04508f3a56");
        System.out.println(result.getCmsPage());
    }
}