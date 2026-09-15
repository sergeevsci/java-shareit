package ru.practicum.shareit.gateway.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.gateway.request.dto.ItemRequestDto;

public class ItemRequestClient extends BaseClient {
    public ItemRequestClient(String serverUrl, RestTemplate rest) {
        super(serverUrl, rest);
    }

    public ResponseEntity<Object> createRequest(long userId, ItemRequestDto itemRequestDto) {
        return post("/requests", userId, itemRequestDto);
    }

    public ResponseEntity<Object> getRequests(long userId) {
        return get("/requests", userId);
    }

    public ResponseEntity<Object> getAllRequests(long userId) {
        return get("/requests/all", userId);
    }

    public ResponseEntity<Object> getRequest(long userId, long requestId) {
        return get("/requests/" + requestId, userId);
    }
}