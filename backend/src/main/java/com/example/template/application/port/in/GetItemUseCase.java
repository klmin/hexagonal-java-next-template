package com.example.template.application.port.in;

import com.example.template.domain.item.Item;

import java.util.List;

public interface GetItemUseCase {

    Item getItem(long id);

    List<Item> getItems();
}
