package com.example.template;

import org.junit.jupiter.api.Test;
import org.springframework.boot.micrometer.metrics.test.autoconfigure.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "management.server.port=0")
@AutoConfigureMetrics
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class ObservabilityTests {

    @LocalServerPort
    private int port;

    @LocalManagementPort
    private int managementPort;

    @Test
    void exposesPrometheusMetricsOnManagementPort() {
        RestClient.create("http://localhost:" + port).get().uri("/api/items").retrieve().toBodilessEntity();

        String metrics = RestClient.create("http://localhost:" + managementPort)
                .get().uri("/actuator/prometheus")
                .retrieve().body(String.class);

        assertThat(metrics)
                .contains("http_server_requests_seconds_bucket")
                .contains("uri=\"/api/items\"")
                .contains("application=\"backend\"")
                .contains("hikaricp_connections_active");
    }
}
