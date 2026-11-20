package com.example.template.item.adapter.in.web;

import com.example.template.item.application.port.in.CreateItemUseCase;
import com.example.template.item.application.port.in.GetItemUseCase;
import com.example.template.item.application.port.in.SearchItemsQuery;
import com.example.template.item.application.port.in.SearchItemsUseCase;
import com.example.template.item.domain.Item;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final CreateItemUseCase createItemUseCase;
    private final GetItemUseCase getItemUseCase;
    private final SearchItemsUseCase searchItemsUseCase;

    public ItemController(CreateItemUseCase createItemUseCase, GetItemUseCase getItemUseCase,
                          SearchItemsUseCase searchItemsUseCase) {
        this.createItemUseCase = createItemUseCase;
        this.getItemUseCase = getItemUseCase;
        this.searchItemsUseCase = searchItemsUseCase;
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

    @GetMapping("/search")
    public ItemPageResponse search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ItemPageResponse.from(searchItemsUseCase.search(new SearchItemsQuery(keyword, page, size)));
    }
}
