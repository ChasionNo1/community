package com.chasion.community.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;

@Configuration
public class ElasticsearchConfig {
    /**
     * localhost:9300 写在配置文件中就可以了
     */
    @Bean
    public RestHighLevelClient restHighLevelClient() {
       ClientConfiguration configuration = (ClientConfiguration) ClientConfiguration.builder()
               .connectedTo("localhost:9200","localhost:9300").build();

        RestHighLevelClient client = RestClients.create(configuration).rest();
        return client;

    }
}
