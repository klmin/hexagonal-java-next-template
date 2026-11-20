package com.example.template.item.application;

import com.example.template.common.error.NotFoundException;

public class ItemNotFoundException extends NotFoundException {

    public ItemNotFoundException(long id) {
        super("item not found: " + id);
    }
}
