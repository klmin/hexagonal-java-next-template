package com.example.template.item.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.template.TestcontainersConfiguration;
import com.example.template.item.domain.Item;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, ItemPersistenceAdapter.class})
@Testcontainers(disabledWithoutDocker = true)
class ItemPersistenceAdapterTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z").truncatedTo(ChronoUnit.MICROS);

    @Autowired
    private ItemPersistenceAdapter adapter;

    @Autowired
    private TestEntityManager em;

    @Test
    void saveAndFindById() {
        Item saved = adapter.save(Item.create("노트북", "업무용", NOW));

        assertThat(saved.id()).isNotNull();
        assertThat(adapter.findById(saved.id())).hasValueSatisfying(found -> {
            assertThat(found.name()).isEqualTo("노트북");
            assertThat(found.description()).isEqualTo("업무용");
            assertThat(found.createdAt()).isEqualTo(NOW);
        });
    }

    @Test
    void updateIsFlushedByDirtyCheckingWithoutSave() {
        Item saved = adapter.save(Item.create("노트북", "업무용", NOW));
        em.flush();
        em.clear();

        adapter.update(adapter.findById(saved.id()).orElseThrow().update("데스크톱", null));
        em.flush();
        em.clear();

        assertThat(adapter.findById(saved.id())).hasValueSatisfying(found -> {
            assertThat(found.name()).isEqualTo("데스크톱");
            assertThat(found.description()).isNull();
            assertThat(found.createdAt()).isEqualTo(NOW);
        });
    }

    @Test
    void deleteByIdReportsWhetherItExisted() {
        Item saved = adapter.save(Item.create("노트북", null, NOW));

        assertThat(adapter.deleteById(saved.id())).isTrue();
        assertThat(adapter.deleteById(saved.id())).isFalse();
        assertThat(adapter.findById(saved.id())).isEmpty();
    }
}
