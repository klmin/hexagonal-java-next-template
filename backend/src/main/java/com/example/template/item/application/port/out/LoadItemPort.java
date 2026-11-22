package com.example.template.item.application.port.out;

import com.example.template.item.domain.Item;

import java.util.Optional;

public interface LoadItemPort {

    Optional<Item> findById(long id);
}
