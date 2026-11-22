package com.example.template.item.adapter.in.web;

import com.example.template.item.application.port.in.ItemListResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record ItemListResponse(
        @Schema(requiredMode = REQUIRED) List<ItemResponse> items,
        @Schema(requiredMode = REQUIRED, types = {"string", "null"},
                description = "다음 페이지 커서. 마지막 페이지면 null") String nextCursor
) {

    static ItemListResponse from(ItemListResult result) {
        return new ItemListResponse(
                result.items().stream().map(ItemResponse::from).toList(),
                ItemCursor.encode(result.nextCursor()));
    }
}
