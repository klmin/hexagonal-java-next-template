package com.example.template.item.adapter.in.web;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.example.template.item.application.port.in.ItemSearchResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ItemPageResponse(
        @Schema(requiredMode = REQUIRED) List<ItemResponse> items,
        @Schema(requiredMode = REQUIRED) long totalCount,
        @Schema(requiredMode = REQUIRED) int page,
        @Schema(requiredMode = REQUIRED) int size) {

    static ItemPageResponse from(ItemSearchResult result) {
        return new ItemPageResponse(
                result.items().stream().map(ItemResponse::from).toList(),
                result.totalCount(),
                result.page(),
                result.size());
    }
}
