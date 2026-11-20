package com.example.template.adapter.out.persistence.item;

import com.example.template.TestcontainersConfiguration;
import com.example.template.domain.item.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실제 PostgreSQL(Testcontainers)과 Flyway 마이그레이션으로 영속성 어댑터를 검증한다.
 * Docker가 없으면 건너뛴다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, ItemPersistenceAdapter.class})
@Testcontainers(disabledWithoutDocker = true)
class ItemPersistenceAdapterTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z").truncatedTo(ChronoUnit.MICROS);

    @Autowired
    private ItemPersistenceAdapter adapter;

    @Test
    void saveAndFindById() {
        Item saved = adapter.save(Item.create("노트북", "업무용", NOW));

        assertThat(saved.id()).isNotNull();
        assertThat(adapter.findById(saved.id()))
                .hasValueSatisfying(found -> {
                    assertThat(found.name()).isEqualTo("노트북");
                    assertThat(found.description()).isEqualTo("업무용");
                    assertThat(found.createdAt()).isEqualTo(NOW);
                });
    }

    @Test
    void findAllReturnsNewestFirst() {
        Item first = adapter.save(Item.create("a", null, NOW));
        Item second = adapter.save(Item.create("b", null, NOW));

        assertThat(adapter.findAll()).extracting(Item::id).containsExactly(second.id(), first.id());
    }
}
