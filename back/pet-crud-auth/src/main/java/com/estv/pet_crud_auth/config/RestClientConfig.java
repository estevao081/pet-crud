package com.estv.pet_crud_auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient userServiceClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }
}