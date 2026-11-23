package com.example.template.item.application.service;

import com.example.template.item.application.port.in.ItemListResult;
import com.example.template.item.application.port.in.ItemSearchResult;
import com.example.template.item.application.port.in.ListItemsQuery;
import com.example.template.item.application.port.in.ListItemsUseCase;
import com.example.template.item.application.port.in.SearchItemsQuery;
import com.example.template.item.application.port.in.SearchItemsUseCase;
import com.example.template.item.application.port.out.ItemPageResult;
import com.example.template.item.application.port.out.ListItemsPort;
import com.example.template.item.application.port.out.SearchItemsPort;
import com.example.template.item.domain.Item;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemSearchService implements SearchItemsUseCase, ListItemsUseCase {

    private final SearchItemsPort searchItemsPort;
    private final ListItemsPort listItemsPort;

    public ItemSearchService(SearchItemsPort searchItemsPort, ListItemsPort listItemsPort) {
        this.searchItemsPort = searchItemsPort;
        this.listItemsPort = listItemsPort;
    }

    // size + 1 개를 읽어서 다음 페이지가 있는지 판단한다 (count 쿼리 없음)
    @Override
    public ItemListResult list(ListItemsQuery query) {
        List<Item> rows = listItemsPort.findBefore(query.cursor(), query.size() + 1);
        if (rows.size() <= query.size()) {
            return new ItemListResult(rows, null);
        }
        List<Item> page = rows.subList(0, query.size());
        return new ItemListResult(List.copyOf(page), page.getLast().id());
    }

    @Override
    public ItemSearchResult search(SearchItemsQuery query) {
        ItemPageResult page = searchItemsPort.search(query.keyword(), (long) query.page() * query.size(), query.size());
        return new ItemSearchResult(page.items(), page.totalCount(), query.page(), query.size());
    }
}
