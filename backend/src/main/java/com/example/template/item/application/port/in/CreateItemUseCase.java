package com.example.template.item.application.port.in;

import com.example.template.item.domain.Item;

public interface CreateItemUseCase {

    Item create(CreateItemCommand command);
}
