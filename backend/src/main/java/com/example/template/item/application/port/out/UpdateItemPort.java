package com.example.template.item.application.port.out;

import com.example.template.item.domain.Item;

public interface UpdateItemPort {

    /** 이미 저장된 Item의 변경 내용을 반영한다. */
    void update(Item item);
}
