package com.example.template.application.exception;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(long id) {
        super("item not found: " + id);
    }
}
