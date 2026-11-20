package com.example.template.common.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ClockConfigTest {

    @Test
    void clockHasMicrosecondPrecisionLikePostgres() {
        Clock clock = new ClockConfig().clock();

        for (int i = 0; i < 100; i++) {
            Instant now = clock.instant();
            assertThat(now.getNano() % 1_000).isZero();
        }
    }
}
