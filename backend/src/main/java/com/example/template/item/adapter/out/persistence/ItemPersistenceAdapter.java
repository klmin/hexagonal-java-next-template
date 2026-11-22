package com.example.template.item.adapter.out.persistence;

import com.example.template.item.application.port.out.DeleteItemPort;
import com.example.template.item.application.port.out.LoadItemPort;
import com.example.template.item.application.port.out.SaveItemPort;
import com.example.template.item.application.port.out.UpdateItemPort;
import com.example.template.item.domain.Item;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
class ItemPersistenceAdapter implements SaveItemPort, LoadItemPort, UpdateItemPort, DeleteItemPort {

    private final ItemJpaRepository repository;

    ItemPersistenceAdapter(ItemJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Item save(Item item) {
        ItemJpaEntity saved = repository.save(toEntity(item));
        return toDomain(saved);
    }

    // 영속 상태 엔티티에 값만 옮긴다. save() 없이 트랜잭션 커밋 때 더티 체킹으로 UPDATE 된다.
    // 같은 트랜잭션에서 방금 조회한 엔티티는 1차 캐시에서 나오므로 SELECT가 다시 나가지 않는다
    @Override
    public void update(Item item) {
        ItemJpaEntity entity = repository.findById(item.id())
                .orElseThrow(() -> new IllegalStateException("item not found: " + item.id()));
        entity.update(item.name(), item.description());
    }

    @Override
    public boolean deleteById(long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
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
