package com.project.teama_be.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient sendBirdWebClient(
            @Value("${sendbird.app-id}") String appId,
            @Value("${sendbird.api-token}") String apiToken) {

        String baseUrl = "https://api-" + appId + ".sendbird.com/v3";

        // 메모리 버퍼 크기 설정 (대용량 응답을 처리하기 위해)
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
                .build();

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Api-Token", apiToken)
                .exchangeStrategies(strategies)
                .build();
    }
}
