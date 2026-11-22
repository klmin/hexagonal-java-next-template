package com.example.template.item.domain;

import com.example.template.common.error.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createTrimsNameAndDescription() {
        Item item = Item.create("  노트북  ", "  설명  ", NOW);

        assertThat(item.id()).isNull();
        assertThat(item.name()).isEqualTo("노트북");
        assertThat(item.description()).isEqualTo("설명");
        assertThat(item.createdAt()).isEqualTo(NOW);
    }

    @Test
    void blankDescriptionBecomesNull() {
        assertThat(Item.create("이름", "   ", NOW).description()).isNull();
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Item.create(" ", null, NOW))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void rejectsTooLongName() {
        String name = "a".repeat(Item.NAME_MAX_LENGTH + 1);

        assertThatThrownBy(() -> Item.create(name, null, NOW))
                .isInstanceOf(DomainValidationException.class);
    }

    @Test
    void restoreRequiresId() {
        assertThatThrownBy(() -> Item.restore(null, "이름", null, NOW))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateKeepsIdAndCreatedAt() {
        Item item = Item.restore(1L, "노트북", "업무용", NOW);

        Item updated = item.update("  데스크톱 ", null);

        assertThat(updated.id()).isEqualTo(1L);
        assertThat(updated.createdAt()).isEqualTo(NOW);
        assertThat(updated.name()).isEqualTo("데스크톱");
        assertThat(updated.description()).isNull();
        assertThat(item.name()).isEqualTo("노트북");
    }

    @Test
    void updateValidatesLikeCreate() {
        Item item = Item.restore(1L, "노트북", null, NOW);

        assertThatThrownBy(() -> item.update(" ", null)).isInstanceOf(DomainValidationException.class);
    }

    @Test
    void unsavedItemCannotBeUpdated() {
        assertThatThrownBy(() -> Item.create("노트북", null, NOW).update("a", null))
                .isInstanceOf(IllegalStateException.class);
    }
}
