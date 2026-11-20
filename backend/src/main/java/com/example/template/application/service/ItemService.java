package com.example.template.application.service;

import com.example.template.application.exception.ItemNotFoundException;
import com.example.template.application.port.in.CreateItemCommand;
import com.example.template.application.port.in.CreateItemUseCase;
import com.example.template.application.port.in.GetItemUseCase;
import com.example.template.application.port.out.LoadItemPort;
import com.example.template.application.port.out.SaveItemPort;
import com.example.template.domain.item.Item;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService implements CreateItemUseCase, GetItemUseCase {

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
        Item item = Item.create(command.name(), command.description(), clock.instant());
        return saveItemPort.save(item);
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
