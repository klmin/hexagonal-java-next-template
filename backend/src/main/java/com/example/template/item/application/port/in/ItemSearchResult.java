package com.example.template.item.application.port.in;

import com.example.template.item.domain.Item;

import java.util.List;

public record ItemSearchResult(List<Item> items, long totalCount, int page, int size) {
}
