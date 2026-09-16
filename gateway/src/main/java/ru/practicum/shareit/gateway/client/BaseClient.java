package ru.practicum.shareit.gateway.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

public abstract class BaseClient {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final String serverUrl;
    protected final RestTemplate rest;

    protected BaseClient(String serverUrl, RestTemplate rest) {
        this.serverUrl = serverUrl;
        this.rest = rest;
    }

    protected ResponseEntity<Object> get(String path) {
        return makeAndSendRequest(HttpMethod.GET, path, null, Map.of(), null);
    }

    protected ResponseEntity<Object> get(String path, Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, null, parameters, null);
    }

    protected ResponseEntity<Object> get(String path, long userId) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, Map.of(), null);
    }

    protected ResponseEntity<Object> get(String path, long userId, Map<String, Object> parameters) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, parameters, null);
    }

    protected ResponseEntity<Object> post(String path, Object body) {
        return makeAndSendRequest(HttpMethod.POST, path, null, Map.of(), body);
    }

    protected ResponseEntity<Object> post(String path, long userId, Object body) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, Map.of(), body);
    }

    protected ResponseEntity<Object> patch(String path, Object body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, null, Map.of(), body);
    }

    protected ResponseEntity<Object> patch(String path, long userId, Object body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, Map.of(), body);
    }

    protected ResponseEntity<Object> patch(String path, long userId, Map<String, Object> parameters, Object body) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, parameters, body);
    }

    protected ResponseEntity<Object> delete(String path) {
        return makeAndSendRequest(HttpMethod.DELETE, path, null, Map.of(), null);
    }

    protected ResponseEntity<Object> delete(String path, long userId) {
        return makeAndSendRequest(HttpMethod.DELETE, path, userId, Map.of(), null);
    }

    private ResponseEntity<Object> makeAndSendRequest(HttpMethod method,
                                                       String path,
                                                       @Nullable Long userId,
                                                       Map<String, Object> parameters,
                                                       @Nullable Object body) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(serverUrl + path);
        parameters.forEach(builder::queryParam);
        String url = builder.build().encode().toUriString();
        return rest.exchange(url, method, requestEntity, Object.class);
    }

    private HttpHeaders defaultHeaders(@Nullable Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set(USER_ID_HEADER, String.valueOf(userId));
        }
        return headers;
    }
}
