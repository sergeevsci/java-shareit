package ru.practicum.shareit.gateway.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.gateway.client.ItemClient;
import ru.practicum.shareit.gateway.item.dto.CommentRequestDto;
import ru.practicum.shareit.gateway.item.dto.ItemRequestDto;

import java.util.Collections;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @Valid @RequestBody ItemRequestDto itemDto) {
        log.info("Запрос на создание вещи: userId={}, название={}", userId, itemDto.getName());
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody ItemRequestDto itemDto) {
        log.info("Запрос на обновление вещи: userId={}, itemId={}", userId, itemId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                           @PathVariable Long itemId) {
        log.info("Запрос на получение вещи: userId={}, itemId={}", userId, itemId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getByOwner(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Запрос на получение вещей владельца: userId={}", userId);
        return itemClient.getItemsByOwner(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @RequestParam String text) {
        log.info("Запрос на поиск вещей: userId={}, текст={}", userId, text);
        if (text == null || text.isBlank()) {
            log.info("Поиск вещей пропущен: пустой текст запроса");
            return ResponseEntity.ok(Collections.emptyList());
        }
        return itemClient.searchItems(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentRequestDto commentDto) {
        log.info("Запрос на добавление комментария: userId={}, itemId={}", userId, itemId);
        return itemClient.addComment(userId, itemId, commentDto);
    }
}
