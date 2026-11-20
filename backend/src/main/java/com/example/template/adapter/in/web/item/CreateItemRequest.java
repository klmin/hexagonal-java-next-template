package com.example.template.adapter.in.web.item;

import com.example.template.application.port.in.CreateItemCommand;
import com.example.template.domain.item.Item;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateItemRequest(
        @NotBlank @Size(max = Item.NAME_MAX_LENGTH) String name,
        @Size(max = Item.DESCRIPTION_MAX_LENGTH) String description
) {

    CreateItemCommand toCommand() {
        return new CreateItemCommand(name, description);
    }
}
