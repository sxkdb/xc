package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TeachPlanMapperTest {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Test
    public void test() throws Exception {
        TeachplanNode teachplanList = teachplanMapper.findTeachplanList("4028e581617f945f01617f9dabc40000");
        System.out.println(teachplanList);
    }

}
