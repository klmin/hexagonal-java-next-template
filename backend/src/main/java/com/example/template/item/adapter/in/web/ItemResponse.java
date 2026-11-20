package com.example.template.item.adapter.in.web;

import com.example.template.item.domain.Item;

import java.time.Instant;

public record ItemResponse(Long id, String name, String description, Instant createdAt) {

    static ItemResponse from(Item item) {
        return new ItemResponse(item.id(), item.name(), item.description(), item.createdAt());
    }
}
