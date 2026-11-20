package com.example.template;

import org.springframework.boot.SpringApplication;

/**
 * Docker Compose 대신 Testcontainers로 DB를 띄워 로컬 실행할 때 사용한다.
 * ./gradlew bootTestRun
 */
public class TestBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(BackendApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
