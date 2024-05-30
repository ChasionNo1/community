package com.chasion.community;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class CommunityServletInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // 启动这个项目，给tomcat 的接口
        return builder.sources(CommunityApplication.class);
    }
}
