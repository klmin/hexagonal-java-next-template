package com.example.template.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.Test;

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
