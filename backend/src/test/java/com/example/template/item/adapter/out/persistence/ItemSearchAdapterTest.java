package com.example.template.item.adapter.out.persistence;

import com.example.template.TestcontainersConfiguration;
import com.example.template.item.application.port.in.ItemSearchResult;
import com.example.template.item.application.port.in.SearchItemsQuery;
import com.example.template.item.domain.Item;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.example.template.common.jooq.Tables.ITEMS;
import static org.assertj.core.api.Assertions.assertThat;

@JooqTest
@Import({TestcontainersConfiguration.class, ItemSearchAdapter.class})
@Testcontainers(disabledWithoutDocker = true)
class ItemSearchAdapterTest {

    private static final OffsetDateTime NOW = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @Autowired
    private DSLContext dsl;

    @Autowired
    private ItemSearchAdapter adapter;

    @BeforeEach
    void setUp() {
        for (String name : new String[]{"노트북", "노트", "마우스", "Notebook Stand"}) {
            dsl.insertInto(ITEMS)
                    .set(ITEMS.NAME, name)
                    .set(ITEMS.CREATED_AT, NOW)
                    .execute();
        }
    }

    @Test
    void searchesByKeywordIgnoringCase() {
        ItemSearchResult result = adapter.search(new SearchItemsQuery("NOTE", 0, 20));

        assertThat(result.items()).extracting(Item::name).containsExactly("Notebook Stand");
        assertThat(result.totalCount()).isEqualTo(1);
    }

    @Test
    void pagesNewestFirst() {
        ItemSearchResult first = adapter.search(new SearchItemsQuery(null, 0, 3));
        ItemSearchResult second = adapter.search(new SearchItemsQuery(null, 1, 3));

        assertThat(first.totalCount()).isEqualTo(4);
        assertThat(first.items()).extracting(Item::name).containsExactly("Notebook Stand", "마우스", "노트");
        assertThat(second.items()).extracting(Item::name).containsExactly("노트북");
    }
}
