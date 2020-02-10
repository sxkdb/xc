package com.xuecheng.manage_cms;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class StringUtilsTest {

    @Test
    public void test() throws Exception {

        System.out.println(StringUtils.isEmpty(null));      // true
        System.out.println(StringUtils.isEmpty(""));        // true
        System.out.println(StringUtils.isEmpty("    "));       // false

        System.out.println(StringUtils.isBlank(null));      // true
        System.out.println(StringUtils.isBlank(""));        // true
        System.out.println(StringUtils.isBlank("     "));       // true


    }

}
