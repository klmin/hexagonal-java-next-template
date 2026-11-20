package com.example.template.common.config;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration(proxyBeanMethods = false)
public class ObservationConfig {

    // 메트릭 수집(/actuator/prometheus) 요청은 15초마다 오므로 관측하지 않는다.
    // 트레이스뿐 아니라 http.server.requests 메트릭에서도 /actuator 요청이 빠진다
    @Bean
    ObservationPredicate skipActuatorObservations() {
        return (name, context) -> !(context instanceof ServerRequestObservationContext request
                && request.getCarrier().getRequestURI().startsWith("/actuator"));
    }
}
