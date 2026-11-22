package com.example.template.item.application.port.out;

public interface DeleteItemPort {

    /** 삭제했으면 true, 없었으면 false. */
    boolean deleteById(long id);
}
