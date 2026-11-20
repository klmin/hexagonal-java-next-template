package com.example.template.adapter.in.web.item;

import com.example.template.application.exception.ItemNotFoundException;
import com.example.template.application.port.in.CreateItemCommand;
import com.example.template.application.port.in.CreateItemUseCase;
import com.example.template.application.port.in.GetItemUseCase;
import com.example.template.domain.item.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateItemUseCase createItemUseCase;

    @MockitoBean
    private GetItemUseCase getItemUseCase;

    @Test
    void createReturns201WithLocation() throws Exception {
        given(createItemUseCase.create(any(CreateItemCommand.class)))
                .willReturn(Item.restore(1L, "노트북", null, NOW));

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "노트북"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/items/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("노트북"));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void getReturns404WhenMissing() throws Exception {
        given(getItemUseCase.getItem(99L)).willThrow(new ItemNotFoundException(99L));

        mockMvc.perform(get("/api/items/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("item not found: 99"));
    }

    @Test
    void listReturnsItems() throws Exception {
        given(getItemUseCase.getItems()).willReturn(List.of(Item.restore(1L, "a", null, NOW)));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("a"));
    }
}
