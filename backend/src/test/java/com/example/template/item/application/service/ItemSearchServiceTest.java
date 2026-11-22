package com.example.template.item.application.service;

import com.example.template.item.application.port.in.ItemListResult;
import com.example.template.item.application.port.in.ListItemsQuery;
import com.example.template.item.application.port.out.ListItemsPort;
import com.example.template.item.domain.Item;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class ItemSearchServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    // id 1..5 가 저장돼 있는 저장소
    private final ListItemsPort store = (beforeId, limit) -> LongStream.rangeClosed(1, 5)
            .filter(id -> beforeId == null || id < beforeId)
            .boxed()
            .sorted(Comparator.reverseOrder())
            .limit(limit)
            .map(id -> Item.restore(id, "item-" + id, null, NOW))
            .toList();

    private final ItemSearchService service = new ItemSearchService((query) -> null, store);

    @Test
    void firstPageReturnsNewestAndNextCursor() {
        ItemListResult result = service.list(new ListItemsQuery(null, 2));

        assertThat(result.items()).extracting(Item::id).containsExactly(5L, 4L);
        assertThat(result.nextCursor()).isEqualTo(4L);
    }

    @Test
    void nextPageStartsAfterCursor() {
        ItemListResult result = service.list(new ListItemsQuery(4L, 2));

        assertThat(result.items()).extracting(Item::id).containsExactly(3L, 2L);
        assertThat(result.nextCursor()).isEqualTo(2L);
    }

    @Test
    void lastPageHasNoNextCursor() {
        ItemListResult result = service.list(new ListItemsQuery(2L, 2));

        assertThat(result.items()).extracting(Item::id).containsExactly(1L);
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    void exactlyFullLastPageHasNoNextCursor() {
        ItemListResult result = service.list(new ListItemsQuery(null, 5));

        assertThat(result.items()).hasSize(5);
        assertThat(result.nextCursor()).isNull();
    }

    @Test
    void walkingAllPagesVisitsEveryItemOnce() {
        List<Long> seen = new ArrayList<>();
        Long cursor = null;
        do {
            ItemListResult page = service.list(new ListItemsQuery(cursor, 2));
            page.items().forEach(item -> seen.add(item.id()));
            cursor = page.nextCursor();
        } while (cursor != null);

        assertThat(seen).containsExactly(5L, 4L, 3L, 2L, 1L);
    }
}
