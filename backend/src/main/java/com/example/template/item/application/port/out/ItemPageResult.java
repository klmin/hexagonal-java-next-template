package com.example.template.item.application.port.out;

import com.example.template.item.domain.Item;

import java.util.List;

// 검색 포트의 결과. 유스케이스 결과(ItemSearchResult, port.in)와 분리해서
// API 모양(page 번호 등)이 바뀌어도 DB 어댑터는 바뀌지 않게 한다
public record ItemPageResult(List<Item> items, long totalCount) {
}
