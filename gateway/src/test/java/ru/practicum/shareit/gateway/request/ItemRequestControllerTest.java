package ru.practicum.shareit.gateway.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import ru.practicum.shareit.gateway.client.ItemRequestClient;
import ru.practicum.shareit.gateway.request.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void createRequestReturnsRequest() throws Exception {
        when(itemRequestClient.createRequest(eq(1L), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "description", "Need a drill")));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Need a drill\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createRequestWithBlankDescriptionReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequestWithoutUserIdHeaderReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Need a drill\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByUserReturnsRequests() throws Exception {
        when(itemRequestClient.getRequests(1L))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1, "description", "Need a drill"))));

        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllReturnsRequests() throws Exception {
        when(itemRequestClient.getAllRequests(2L))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1, "description", "Need a drill"))));

        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Need a drill"));
    }

    @Test
    void getByIdReturnsRequest() throws Exception {
        when(itemRequestClient.getRequest(1L, 1L))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "description", "Need a drill")));

        mockMvc.perform(get("/requests/{requestId}", 1L).header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void serverNotFoundErrorIsForwarded() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        when(itemRequestClient.getRequest(1L, 999L)).thenThrow(HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                headers,
                "{\"error\":\"Request not found\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8));

        mockMvc.perform(get("/requests/{requestId}", 999L).header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Request not found\"}"));
    }
}