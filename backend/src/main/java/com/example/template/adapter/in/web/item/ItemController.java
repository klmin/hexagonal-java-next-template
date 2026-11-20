package com.example.template.adapter.in.web.item;

import com.example.template.application.port.in.CreateItemUseCase;
import com.example.template.application.port.in.GetItemUseCase;
import com.example.template.domain.item.Item;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final CreateItemUseCase createItemUseCase;
    private final GetItemUseCase getItemUseCase;

    public ItemController(CreateItemUseCase createItemUseCase, GetItemUseCase getItemUseCase) {
        this.createItemUseCase = createItemUseCase;
        this.getItemUseCase = getItemUseCase;
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody CreateItemRequest request) {
        Item item = createItemUseCase.create(request.toCommand());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(item.id())
                .toUri();
        return ResponseEntity.created(location).body(ItemResponse.from(item));
    }

    @GetMapping("/{id}")
    public ItemResponse get(@PathVariable long id) {
        return ItemResponse.from(getItemUseCase.getItem(id));
    }

    @GetMapping
    public List<ItemResponse> list() {
        return getItemUseCase.getItems().stream().map(ItemResponse::from).toList();
    }
}
