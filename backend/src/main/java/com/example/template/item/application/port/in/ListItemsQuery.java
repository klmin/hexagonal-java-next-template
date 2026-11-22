package com.example.template.item.application.port.in;

import com.example.template.common.error.DomainValidationException;

/**
 * 커서 기반 목록 조회. 최신순(id 내림차순)으로 cursor 보다 작은 id 부터 size 개를 가져온다.
 *
 * @param cursor 이전 페이지 마지막 항목의 id. 첫 페이지는 null
 */
public record ListItemsQuery(Long cursor, int size) {

    public static final int MAX_SIZE = 100;

    public ListItemsQuery {
        if (cursor != null && cursor <= 0) {
            throw new DomainValidationException("cursor is invalid");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new DomainValidationException("size must be between 1 and " + MAX_SIZE);
        }
    }
}
