package com.example.template.item.application.port.in;

import com.example.template.common.error.DomainValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListItemsQueryTest {

    @Test
    void rejectsInvalidValues() {
        assertThatThrownBy(() -> new ListItemsQuery(0L, 20)).isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new ListItemsQuery(null, 0)).isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new ListItemsQuery(null, ListItemsQuery.MAX_SIZE + 1))
                .isInstanceOf(DomainValidationException.class);
    }
}
