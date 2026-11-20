package com.example.template.adapter.in.web.item;

import com.example.template.domain.item.Item;

import java.time.Instant;

public record ItemResponse(Long id, String name, String description, Instant createdAt) {

    static ItemResponse from(Item item) {
        return new ItemResponse(item.id(), item.name(), item.description(), item.createdAt());
    }
}
