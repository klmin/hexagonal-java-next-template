package com.example.template.item.adapter.in.web;

import com.example.template.item.application.port.in.ItemSearchResult;

import java.util.List;

public record ItemPageResponse(List<ItemResponse> items, long totalCount, int page, int size) {

    static ItemPageResponse from(ItemSearchResult result) {
        return new ItemPageResponse(
                result.items().stream().map(ItemResponse::from).toList(),
                result.totalCount(),
                result.page(),
                result.size());
    }
}
