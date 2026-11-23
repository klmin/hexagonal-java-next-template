package com.example.template;

import org.springframework.boot.SpringApplication;

// ./gradlew bootTestRun (DB를 Testcontainers로 띄워서 실행)
public class TestBackendApplication {

    public static void main(String[] args) {
        SpringApplication.from(BackendApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
