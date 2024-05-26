package com.chasion.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WKConfig {

    private static final Logger logger = LoggerFactory.getLogger(WKConfig.class);


    @Value("${wk.image.storage}")
    private String storage;

    // 在服务器启动时创建路径

    @PostConstruct
    public void init(){
        // 创建路径
        File file = new File(storage);
        if(!file.exists()){
            file.mkdir();
            logger.info("Creating storage folder:" + storage);
        }
    }
}
