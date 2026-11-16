package com.estv.pet_crud_user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient userServiceClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8082")
                .build();
    }
}
