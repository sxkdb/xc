package com.xuecheng.manage_cms_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = "com.xuecheng.framework.domain.cms")  //扫描实体类
@ComponentScan(basePackages = {"com.xuecheng.framework"}) //扫描通用工程common
@ComponentScan(basePackages = {"com.xuecheng.manage_cms_client"}) //扫描本项目的类
public class ManageCmsClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManageCmsClientApplication.class,args);
    }

}
