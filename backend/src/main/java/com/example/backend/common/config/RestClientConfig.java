package com.example.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient nominatimRestClient(
            @Value("${app.nominatim.url}") String nominatimUrl,
            @Value("${app.nominatim.user-agent}") String userAgent) {
        return RestClient.builder()
                .baseUrl(nominatimUrl)
                .defaultHeader("User-Agent", userAgent)
                .build();
    }
}