package com.example.template.item.adapter.in.web;

import com.example.template.common.error.DomainValidationException;
import com.example.template.item.application.ItemNotFoundException;
import com.example.template.item.application.port.in.CreateItemCommand;
import com.example.template.item.application.port.in.CreateItemUseCase;
import com.example.template.item.application.port.in.GetItemUseCase;
import com.example.template.item.application.port.in.ItemSearchResult;
import com.example.template.item.application.port.in.SearchItemsQuery;
import com.example.template.item.application.port.in.SearchItemsUseCase;
import com.example.template.item.domain.Item;
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

    @MockitoBean
    private SearchItemsUseCase searchItemsUseCase;

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
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
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

    @Test
    void searchReturnsPage() throws Exception {
        given(searchItemsUseCase.search(new SearchItemsQuery("노트", 0, 20)))
                .willReturn(new ItemSearchResult(List.of(Item.restore(1L, "노트북", null, NOW)), 1, 0, 20));

        mockMvc.perform(get("/api/items/search").param("keyword", "노트"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("노트북"))
                .andExpect(jsonPath("$.totalCount").value(1));
    }

    @Test
    void searchRejectsTooLargePageSize() throws Exception {
        mockMvc.perform(get("/api/items/search").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.detail").value("입력값을 확인해 주세요."))
                .andExpect(jsonPath("$.errors[0].field").value("size"));
    }

    @Test
    void createReturns400WhenDomainRuleViolated() throws Exception {
        given(createItemUseCase.create(any(CreateItemCommand.class)))
                .willThrow(new DomainValidationException("이미 있는 이름입니다."));

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "노트북"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.detail").value("이미 있는 이름입니다."));
    }

    @Test
    void createRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void getRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/items/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void unexpectedErrorReturns500ProblemWithoutInternalMessage() throws Exception {
        given(getItemUseCase.getItems()).willThrow(new IllegalStateException("db password=secret"));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.detail").value("서버 오류가 발생했습니다."));
    }
}
