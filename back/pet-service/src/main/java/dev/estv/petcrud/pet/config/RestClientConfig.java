package dev.estv.petcrud.pet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    /** Timeouts curtos: a listagem de pets é pública e não pode ficar presa a um user-service lento. */
    @Bean
    public RestClient userRestClient(@Value("${services.user.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(1));
        factory.setReadTimeout(Duration.ofSeconds(3));
        return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
    }
}
