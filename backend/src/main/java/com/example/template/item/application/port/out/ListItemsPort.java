package com.example.template.item.application.port.out;

import com.example.template.item.domain.Item;
import java.util.List;

public interface ListItemsPort {

    /** id 내림차순으로 beforeId 보다 작은 항목을 최대 limit 개. beforeId 가 null 이면 처음부터. */
    List<Item> findBefore(Long beforeId, int limit);
}
