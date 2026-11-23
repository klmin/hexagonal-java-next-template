package com.example.template.item.application.service;

import com.example.template.item.application.ItemNotFoundException;
import com.example.template.item.application.port.in.CreateItemCommand;
import com.example.template.item.application.port.in.UpdateItemCommand;
import com.example.template.item.application.port.out.DeleteItemPort;
import com.example.template.item.application.port.out.LoadItemPort;
import com.example.template.item.application.port.out.SaveItemPort;
import com.example.template.item.application.port.out.UpdateItemPort;
import com.example.template.item.domain.Item;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private final InMemoryItemStore store = new InMemoryItemStore();
    private final ItemService service = new ItemService(store, store, store, store, Clock.fixed(NOW, ZoneOffset.UTC));

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
    void updateChangesStoredItem() {
        Item created = service.create(new CreateItemCommand("노트북", "업무용"));

        Item updated = service.update(new UpdateItemCommand(created.id(), "데스크톱", null));

        assertThat(updated.name()).isEqualTo("데스크톱");
        assertThat(service.getItem(created.id()).name()).isEqualTo("데스크톱");
        assertThat(service.getItem(created.id()).createdAt()).isEqualTo(NOW);
    }

    @Test
    void updateThrowsWhenMissing() {
        assertThatThrownBy(() -> service.update(new UpdateItemCommand(99L, "a", null)))
                .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void deleteRemovesItem() {
        Item created = service.create(new CreateItemCommand("노트북", null));

        service.delete(created.id());

        assertThatThrownBy(() -> service.getItem(created.id())).isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void deleteThrowsWhenMissing() {
        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ItemNotFoundException.class);
    }

    private static final class InMemoryItemStore implements SaveItemPort, LoadItemPort, UpdateItemPort, DeleteItemPort {

        private final List<Item> items = new ArrayList<>();
        // 실제 DB 시퀀스처럼 지워도 번호를 재사용하지 않는다 (items.size() + 1 은 삭제 뒤 중복 id 를 만든다)
        private long sequence;

        @Override
        public Item save(Item item) {
            Item saved = Item.restore(++sequence, item.name(), item.description(), item.createdAt());
            items.add(saved);
            return saved;
        }

        @Override
        public Optional<Item> findById(long id) {
            return items.stream().filter(item -> item.id() == id).findFirst();
        }

        @Override
        public void update(Item item) {
            items.replaceAll(existing -> existing.id().equals(item.id()) ? item : existing);
        }

        @Override
        public boolean deleteById(long id) {
            return items.removeIf(item -> item.id() == id);
        }
    }
}
