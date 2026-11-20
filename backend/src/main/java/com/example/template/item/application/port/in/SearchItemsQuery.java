package com.example.template.item.application.port.in;

import com.example.template.common.error.DomainValidationException;

public record SearchItemsQuery(String keyword, int page, int size) {

    public SearchItemsQuery {
        if (page < 0) {
            throw new DomainValidationException("page must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new DomainValidationException("size must be between 1 and 100");
        }
        keyword = (keyword == null || keyword.isBlank()) ? null : keyword.strip();
    }
}
