package com.example.template.item.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.template.common.error.DomainValidationException;
import org.junit.jupiter.api.Test;

class ItemCursorTest {

    @Test
    void roundTrip() {
        String cursor = ItemCursor.encode(42L);

        assertThat(cursor).doesNotContain("42");
        assertThat(ItemCursor.decode(cursor)).isEqualTo(42L);
    }

    @Test
    void nullAndBlankMeanFirstPage() {
        assertThat(ItemCursor.encode(null)).isNull();
        assertThat(ItemCursor.decode(null)).isNull();
        assertThat(ItemCursor.decode(" ")).isNull();
    }

    @Test
    void rejectsTamperedCursor() {
        assertThatThrownBy(() -> ItemCursor.decode("not-base64!")).isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> ItemCursor.decode(ItemCursor.encode(-1L)))
                .isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> ItemCursor.decode("YWJj")).isInstanceOf(DomainValidationException.class);
    }
}
