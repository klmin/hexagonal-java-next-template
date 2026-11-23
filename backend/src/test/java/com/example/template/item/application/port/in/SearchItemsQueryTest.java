package com.example.template.item.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.template.common.error.DomainValidationException;
import org.junit.jupiter.api.Test;

class SearchItemsQueryTest {

    @Test
    void blankKeywordMeansNoFilter() {
        assertThat(new SearchItemsQuery("  ", 0, 20).keyword()).isNull();
        assertThat(new SearchItemsQuery(" 노트 ", 0, 20).keyword()).isEqualTo("노트");
    }

    @Test
    void rejectsInvalidPaging() {
        assertThatThrownBy(() -> new SearchItemsQuery(null, -1, 20)).isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new SearchItemsQuery(null, 0, 0)).isInstanceOf(DomainValidationException.class);
        assertThatThrownBy(() -> new SearchItemsQuery(null, 0, 101)).isInstanceOf(DomainValidationException.class);
    }
}
