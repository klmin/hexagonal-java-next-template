package com.example.template.item.application.port.in;

import com.example.template.item.domain.Item;

public interface UpdateItemUseCase {

    Item update(UpdateItemCommand command);
}
