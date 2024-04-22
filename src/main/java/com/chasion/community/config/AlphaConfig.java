package com.chasion.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration
public class AlphaConfig {

    @Bean
    public SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }
}
