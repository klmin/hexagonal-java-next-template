package com.example.template.item.application.port.out;

import com.example.template.item.domain.Item;

public interface SaveItemPort {

    Item save(Item item);
}
