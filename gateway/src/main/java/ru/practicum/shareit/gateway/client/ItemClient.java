package ru.practicum.shareit.gateway.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.gateway.item.dto.CommentRequestDto;
import ru.practicum.shareit.gateway.item.dto.ItemRequestDto;

import java.util.Map;

public class ItemClient extends BaseClient {
    public ItemClient(String serverUrl, RestTemplate rest) {
        super(serverUrl, rest);
    }

    public ResponseEntity<Object> createItem(long userId, ItemRequestDto itemDto) {
        return post("/items", userId, itemDto);
    }

    public ResponseEntity<Object> updateItem(long userId, long itemId, ItemRequestDto itemDto) {
        return patch("/items/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getItem(long userId, long itemId) {
        return get("/items/" + itemId, userId);
    }

    public ResponseEntity<Object> getItemsByOwner(long userId) {
        return get("/items", userId);
    }

    public ResponseEntity<Object> searchItems(long userId, String text) {
        return get("/items/search", userId, Map.of("text", text));
    }

    public ResponseEntity<Object> addComment(long userId, long itemId, CommentRequestDto commentDto) {
        return post("/items/" + itemId + "/comment", userId, commentDto);
    }
}
