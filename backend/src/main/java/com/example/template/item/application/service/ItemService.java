package com.example.template.item.application.service;

import com.example.template.item.application.ItemNotFoundException;
import com.example.template.item.application.port.in.CreateItemCommand;
import com.example.template.item.application.port.in.CreateItemUseCase;
import com.example.template.item.application.port.in.DeleteItemUseCase;
import com.example.template.item.application.port.in.GetItemUseCase;
import com.example.template.item.application.port.in.UpdateItemCommand;
import com.example.template.item.application.port.in.UpdateItemUseCase;
import com.example.template.item.application.port.out.DeleteItemPort;
import com.example.template.item.application.port.out.LoadItemPort;
import com.example.template.item.application.port.out.SaveItemPort;
import com.example.template.item.application.port.out.UpdateItemPort;
import com.example.template.item.domain.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService implements CreateItemUseCase, GetItemUseCase, UpdateItemUseCase, DeleteItemUseCase {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final SaveItemPort saveItemPort;
    private final LoadItemPort loadItemPort;
    private final UpdateItemPort updateItemPort;
    private final DeleteItemPort deleteItemPort;
    private final Clock clock;

    public ItemService(SaveItemPort saveItemPort, LoadItemPort loadItemPort, UpdateItemPort updateItemPort,
                       DeleteItemPort deleteItemPort, Clock clock) {
        this.saveItemPort = saveItemPort;
        this.loadItemPort = loadItemPort;
        this.updateItemPort = updateItemPort;
        this.deleteItemPort = deleteItemPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Item create(CreateItemCommand command) {
        Item saved = saveItemPort.save(Item.create(command.name(), command.description(), clock.instant()));
        log.info("item created: id={}", saved.id());
        return saved;
    }

    // 조회 -> 도메인 규칙으로 변경 -> 포트로 반영. 반영 방식(JPA 더티 체킹)은 어댑터가 안다
    @Override
    @Transactional
    public Item update(UpdateItemCommand command) {
        Item updated = getItem(command.id()).update(command.name(), command.description());
        updateItemPort.update(updated);
        log.info("item updated: id={}", updated.id());
        return updated;
    }

    @Override
    @Transactional
    public void delete(long id) {
        if (!deleteItemPort.deleteById(id)) {
            throw new ItemNotFoundException(id);
        }
        log.info("item deleted: id={}", id);
    }

    @Override
    public Item getItem(long id) {
        return loadItemPort.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Override
    public List<Item> getItems() {
        return loadItemPort.findAll();
    }
}
