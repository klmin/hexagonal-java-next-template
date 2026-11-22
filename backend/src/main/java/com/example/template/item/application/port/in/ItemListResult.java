package com.example.template.item.application.port.in;

import com.example.template.item.domain.Item;

import java.util.List;

/**
 * @param nextCursor 다음 페이지를 가져올 커서. 마지막 페이지면 null
 */
public record ItemListResult(List<Item> items, Long nextCursor) {
}
