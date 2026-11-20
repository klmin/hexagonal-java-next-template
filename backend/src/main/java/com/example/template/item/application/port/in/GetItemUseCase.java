package com.example.template.item.application.port.in;

import com.example.template.item.domain.Item;

import java.util.List;

public interface GetItemUseCase {

    Item getItem(long id);

    List<Item> getItems();
}
