package com.example.template.item.application.port.out;

import com.example.template.item.application.port.in.ItemSearchResult;
import com.example.template.item.application.port.in.SearchItemsQuery;

public interface SearchItemsPort {

    ItemSearchResult search(SearchItemsQuery query);
}
