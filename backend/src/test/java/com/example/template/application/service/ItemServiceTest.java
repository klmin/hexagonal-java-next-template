package com.example.template.application.service;

import com.example.template.application.exception.ItemNotFoundException;
import com.example.template.application.port.in.CreateItemCommand;
import com.example.template.application.port.out.LoadItemPort;
import com.example.template.application.port.out.SaveItemPort;
import com.example.template.domain.item.Item;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 출력 포트를 인메모리 구현으로 대체해 애플리케이션 계층만 검증한다.
 */
class ItemServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private final InMemoryItemStore store = new InMemoryItemStore();
    private final ItemService service = new ItemService(store, store, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void createAssignsIdAndCurrentTime() {
        Item created = service.create(new CreateItemCommand("노트북", "업무용"));

        assertThat(created.id()).isEqualTo(1L);
        assertThat(created.createdAt()).isEqualTo(NOW);
        assertThat(service.getItem(1L).name()).isEqualTo("노트북");
    }

    @Test
    void getItemThrowsWhenMissing() {
        assertThatThrownBy(() -> service.getItem(99L))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void getItemsReturnsAll() {
        service.create(new CreateItemCommand("a", null));
        service.create(new CreateItemCommand("b", null));

        assertThat(service.getItems()).hasSize(2);
    }

    private static final class InMemoryItemStore implements SaveItemPort, LoadItemPort {

        private final List<Item> items = new ArrayList<>();

        @Override
        public Item save(Item item) {
            Item saved = Item.restore((long) items.size() + 1, item.name(), item.description(), item.createdAt());
            items.add(saved);
            return saved;
        }

        @Override
        public Optional<Item> findById(long id) {
            return items.stream().filter(item -> item.id() == id).findFirst();
        }

        @Override
        public List<Item> findAll() {
            return List.copyOf(items);
        }
    }
}
