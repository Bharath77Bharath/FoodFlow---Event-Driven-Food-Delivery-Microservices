package com.foodflow.paymentservice.Security;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class InternalServiceFeignConfig {

    @Value("${foodflow.internal-api-key}")
    private String internalApiKey;

    @Bean
    public RequestInterceptor internalServiceInterceptor() {

        return requestTemplate -> {
            requestTemplate.header(
                    "X-Internal-Service-Key",
                    internalApiKey
            );
        };
    }
}