package com.chasion.community.config;

import com.chasion.community.controller.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // 配置拦截器
    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

//    @Autowired
//    private LoginRequiredInterceptor loginRequiredInterceptor;

    @Autowired
    private DataInterceptor dataInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/*.css", "/*.js", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif")
                .addPathPatterns("/register", "/login", "logout");

        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/*.css", "/*.js", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif");

//        registry.addInterceptor(loginRequiredInterceptor)
//                .excludePathPatterns("/*.css", "/*.js", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif");

        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/*.css", "/*.js", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif");
        registry.addInterceptor(dataInterceptor)
                .excludePathPatterns("/*.css", "/*.js", "/*.png", "/*.jpg", "/*.jpeg", "/*.gif");
    }


}
