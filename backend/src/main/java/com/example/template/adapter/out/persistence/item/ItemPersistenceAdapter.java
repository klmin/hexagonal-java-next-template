package com.example.template.adapter.out.persistence.item;

import com.example.template.application.port.out.LoadItemPort;
import com.example.template.application.port.out.SaveItemPort;
import com.example.template.domain.item.Item;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class ItemPersistenceAdapter implements SaveItemPort, LoadItemPort {

    private final ItemJpaRepository repository;

    ItemPersistenceAdapter(ItemJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Item save(Item item) {
        ItemJpaEntity saved = repository.save(toEntity(item));
        return toDomain(saved);
    }

    @Override
    public Optional<Item> findById(long id) {
        return repository.findById(id).map(ItemPersistenceAdapter::toDomain);
    }

    @Override
    public List<Item> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(ItemPersistenceAdapter::toDomain)
                .toList();
    }

    private static ItemJpaEntity toEntity(Item item) {
        return new ItemJpaEntity(item.id(), item.name(), item.description(), item.createdAt());
    }

    private static Item toDomain(ItemJpaEntity entity) {
        return Item.restore(entity.getId(), entity.getName(), entity.getDescription(), entity.getCreatedAt());
    }
}
