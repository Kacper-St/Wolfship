package com.example.backend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient nominatimRestClient(
            @Value("${app.nominatim.url}") String nominatimUrl,
            @Value("${app.nominatim.user-agent}") String userAgent) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(nominatimUrl)
                .defaultHeader("User-Agent", userAgent)
                .requestFactory(factory)
                .build();
    }
}