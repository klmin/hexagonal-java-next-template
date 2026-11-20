package com.example.template.application.port.in;

import com.example.template.domain.item.Item;

public interface CreateItemUseCase {

    Item create(CreateItemCommand command);
}
