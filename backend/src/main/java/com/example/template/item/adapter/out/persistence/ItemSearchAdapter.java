package com.example.template.item.adapter.out.persistence;

import com.example.template.item.application.port.in.ItemSearchResult;
import com.example.template.item.application.port.in.SearchItemsQuery;
import com.example.template.item.application.port.out.SearchItemsPort;
import com.example.template.item.domain.Item;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.template.common.jooq.Tables.ITEMS;
import static org.jooq.impl.DSL.noCondition;

// 조회 전용. 저장/수정은 JPA(ItemPersistenceAdapter), 검색 같은 조회는 jOOQ
@Component
class ItemSearchAdapter implements SearchItemsPort {

    private final DSLContext dsl;

    ItemSearchAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public ItemSearchResult search(SearchItemsQuery query) {
        Condition condition = query.keyword() == null
                ? noCondition()
                : ITEMS.NAME.containsIgnoreCase(query.keyword());

        List<Item> items = dsl.selectFrom(ITEMS)
                .where(condition)
                .orderBy(ITEMS.ID.desc())
                .limit(query.size())
                .offset((long) query.page() * query.size())
                .fetch(r -> Item.restore(r.getId(), r.getName(), r.getDescription(), r.getCreatedAt().toInstant()));

        long total = dsl.fetchCount(ITEMS, condition);

        return new ItemSearchResult(items, total, query.page(), query.size());
    }
}
