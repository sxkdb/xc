package com.xuecheng.manage_media;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class TestStringUtils {


    @Test
    public void test() throws Exception {

        System.out.println(StringUtils.isNotEmpty("   "));
        System.out.println(StringUtils.isNotBlank("   "));

    }
}
