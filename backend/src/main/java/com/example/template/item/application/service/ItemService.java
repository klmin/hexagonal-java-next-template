package com.example.template.item.application.service;

import com.example.template.item.application.ItemNotFoundException;
import com.example.template.item.application.port.in.CreateItemCommand;
import com.example.template.item.application.port.in.CreateItemUseCase;
import com.example.template.item.application.port.in.GetItemUseCase;
import com.example.template.item.application.port.out.LoadItemPort;
import com.example.template.item.application.port.out.SaveItemPort;
import com.example.template.item.domain.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService implements CreateItemUseCase, GetItemUseCase {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final SaveItemPort saveItemPort;
    private final LoadItemPort loadItemPort;
    private final Clock clock;

    public ItemService(SaveItemPort saveItemPort, LoadItemPort loadItemPort, Clock clock) {
        this.saveItemPort = saveItemPort;
        this.loadItemPort = loadItemPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Item create(CreateItemCommand command) {
        Item saved = saveItemPort.save(Item.create(command.name(), command.description(), clock.instant()));
        log.info("item created: id={}", saved.id());
        return saved;
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
