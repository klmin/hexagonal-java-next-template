package com.example.template.item.adapter.in.web;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.template.item.domain.Item;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record ItemResponse(
        @Schema(requiredMode = REQUIRED) Long id,
        @Schema(requiredMode = REQUIRED) String name,

        @Schema(
                requiredMode = REQUIRED,
                types = {"string", "null"})
        String description,

        @Schema(requiredMode = REQUIRED) Instant createdAt) {

    static ItemResponse from(Item item) {
        return new ItemResponse(item.id(), item.name(), item.description(), item.createdAt());
    }
}
