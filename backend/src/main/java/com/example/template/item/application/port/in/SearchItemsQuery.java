package com.example.template.item.application.port.in;

public record SearchItemsQuery(String keyword, int page, int size) {

    public SearchItemsQuery {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        keyword = (keyword == null || keyword.isBlank()) ? null : keyword.strip();
    }
}
