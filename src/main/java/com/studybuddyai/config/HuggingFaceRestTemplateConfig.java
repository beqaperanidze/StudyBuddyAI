package com.studybuddyai.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.HttpHeaders;

import java.time.Duration;

@Configuration
public class HuggingFaceRestTemplateConfig {

    @Value("${huggingface.api.key}")
    private String huggingfaceApiKey;

    @Bean
    @Qualifier("huggingFaceRestTemplate")
    public RestTemplate huggingFaceRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        ClientHttpRequestInterceptor apiKeyInterceptor = (request, body, execution) -> {
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + huggingfaceApiKey);
            return execution.execute(request, body);
        };

        return restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(30))
                .interceptors(apiKeyInterceptor)
                .build();
    }
}