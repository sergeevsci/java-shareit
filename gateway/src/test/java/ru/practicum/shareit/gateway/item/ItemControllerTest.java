package ru.practicum.shareit.gateway.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.client.ItemClient;
import ru.practicum.shareit.gateway.item.dto.CommentRequestDto;
import ru.practicum.shareit.gateway.item.dto.ItemRequestDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Test
    void createItemReturnsItem() throws Exception {
        when(itemClient.createItem(any(Long.class), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "name", "Drill")));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Powerful drill\",\"available\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void createItemWithBlankNameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \",\"description\":\"Description\",\"available\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItemWithoutUserIdHeaderReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Powerful drill\",\"available\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItemReturnsUpdatedItem() throws Exception {
        when(itemClient.updateItem(any(Long.class), any(Long.class), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "name", "New drill")));

        mockMvc.perform(patch("/items/{itemId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New drill"));
    }

    @Test
    void getByIdReturnsItem() throws Exception {
        when(itemClient.getItem(1L, 1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1, "name", "Drill")));

        mockMvc.perform(get("/items/{itemId}", 1L).header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByOwnerReturnsItems() throws Exception {
        when(itemClient.getItemsByOwner(1L))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1, "name", "Drill"))));

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void searchWithTextReturnsItems() throws Exception {
        when(itemClient.searchItems(1L, "drill"))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1, "name", "Drill"))));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void searchWithBlankTextReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void searchWithoutUserIdHeaderReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCommentReturnsComment() throws Exception {
        when(itemClient.addComment(any(Long.class), any(Long.class), any(CommentRequestDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "text", "Great item")));

        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Great item\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great item"));
    }

    @Test
    void addCommentWithBlankTextReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/items/{itemId}/comment", 1L)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\" \"}"))
                .andExpect(status().isBadRequest());
    }
}
