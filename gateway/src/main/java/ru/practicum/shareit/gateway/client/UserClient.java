package ru.practicum.shareit.gateway.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.gateway.user.dto.UserRequestDto;

public class UserClient extends BaseClient {
    public UserClient(String serverUrl, RestTemplate rest) {
        super(serverUrl, rest);
    }

    public ResponseEntity<Object> createUser(UserRequestDto userDto) {
        return post("/users", 0, userDto);
    }

    public ResponseEntity<Object> updateUser(Long userId, UserRequestDto userDto) {
        return patch("/users/" + userId, 0, userDto);
    }

    public ResponseEntity<Object> getUser(Long userId) {
        return get("/users/" + userId, 0);
    }

    public ResponseEntity<Object> getUsers() {
        return get("/users", 0);
    }

    public ResponseEntity<Object> deleteUser(Long userId) {
        return delete("/users/" + userId, 0);
    }
}