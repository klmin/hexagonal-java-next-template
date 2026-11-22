package com.example.template.item.adapter.in.web;

import com.example.template.item.application.port.in.UpdateItemCommand;
import com.example.template.item.domain.Item;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateItemRequest(
        @NotBlank @Size(max = Item.NAME_MAX_LENGTH) String name,
        @Size(max = Item.DESCRIPTION_MAX_LENGTH) String description
) {

    UpdateItemCommand toCommand(long id) {
        return new UpdateItemCommand(id, name, description);
    }
}
