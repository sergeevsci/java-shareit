package ru.practicum.shareit.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorHandler {
    private final ObjectMapper objectMapper;

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Object> handleServerError(HttpStatusCodeException exception) {
        String body = exception.getResponseBodyAsString();
        Object errorBody;
        try {
            errorBody = objectMapper.readValue(body, Object.class);
        } catch (JsonProcessingException e) {
            errorBody = body;
        }
        log.warn("Ошибка сервера: статус={}, тело={}", exception.getStatusCode(), body);
        return ResponseEntity.status(exception.getStatusCode()).body(errorBody);
    }
}
