package com.example.template.application.port.out;

import com.example.template.domain.item.Item;

import java.util.List;
import java.util.Optional;

public interface LoadItemPort {

    Optional<Item> findById(long id);

    List<Item> findAll();
}
