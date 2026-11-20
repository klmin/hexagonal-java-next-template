package com.example.template.item.application.service;

import com.example.template.item.application.port.in.ItemSearchResult;
import com.example.template.item.application.port.in.SearchItemsQuery;
import com.example.template.item.application.port.in.SearchItemsUseCase;
import com.example.template.item.application.port.out.SearchItemsPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ItemSearchService implements SearchItemsUseCase {

    private final SearchItemsPort searchItemsPort;

    public ItemSearchService(SearchItemsPort searchItemsPort) {
        this.searchItemsPort = searchItemsPort;
    }

    @Override
    public ItemSearchResult search(SearchItemsQuery query) {
        return searchItemsPort.search(query);
    }
}
