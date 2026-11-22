package com.example.template.item.application.port.in;

import com.example.template.item.domain.Item;

public interface GetItemUseCase {

    Item getItem(long id);
}
