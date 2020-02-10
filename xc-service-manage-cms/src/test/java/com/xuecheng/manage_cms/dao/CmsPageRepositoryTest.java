package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms.ManageCmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManageCmsApplication.class)
public class CmsPageRepositoryTest {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Test
    public void testFindAll() throws Exception {
        List<CmsPage> list = cmsPageRepository.findAll();
    }

    //分页查询
    @Test
    public void testPage() throws Exception {

        int page = 0;
        int size = 5;
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> pages = cmsPageRepository.findAll(pageable);

    }

    //条件查询
    @Test
    public void test3() throws Exception {
        Optional<CmsPage> page = cmsPageRepository.findById("5a7be667d019f14d90a1fb1c");
        if (page.isPresent()) {//是否存在
            CmsPage cmsPage = page.get();
            System.out.println("cmsPage=" + cmsPage);
        }
    }

    //自定义查询条件
    @Test
    public void testFindAllByzdy() throws Exception {
        //分页
        Pageable pageable = PageRequest.of(0, 10);
        //存放查询条件
        CmsPage cmsPage = new CmsPage();
        //添加查询别名的条件
        cmsPage.setPageAliase("轮播");
        //添加查询模板id的条件
        cmsPage.setTemplateId("5a962bf8b00ffc514038fafa");
        //添加查询站点id的条件
        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
        //这两句可以合到一起
        //条件匹配器  有多种匹配器 ExampleMatcher.GenericPropertyMatchers.contains()实现模糊查询
//        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        //对别名pageAliase使用模糊查询
//        exampleMatcher = exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());

        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        //执行查询
        Page<CmsPage> cmsPagePages = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> content = cmsPagePages.getContent();
        System.out.println(content);
    }
}
