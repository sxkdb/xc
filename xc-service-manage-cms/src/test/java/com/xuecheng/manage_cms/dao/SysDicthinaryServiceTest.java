package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.service.SysDicthinaryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SysDicthinaryServiceTest {

    @Autowired
    private SysDicthinaryService sysDicthinaryService;

    @Test
    public void test() throws Exception {
        SysDictionary byType = sysDicthinaryService.getByType("200");
        System.out.println(byType);
    }


}
