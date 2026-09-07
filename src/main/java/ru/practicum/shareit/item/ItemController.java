package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.validation.Create;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                          @Validated(Create.class) @RequestBody ItemRequestDto itemDto) {
        log.info("Запрос на создание вещи: userId={}, название={}", userId, itemDto.getName());
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemRequestDto itemDto) {
        log.info("Запрос на обновление вещи: userId={}, itemId={}", userId, itemId);
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
                           @PathVariable Long itemId) {
        log.info("Запрос на получение вещи: userId={}, itemId={}", userId, itemId);
        return itemService.getById(userId, itemId);
    }

    @GetMapping
    public Collection<ItemDto> getByOwner(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Запрос на получение вещей владельца: userId={}", userId);
        return itemService.getByOwner(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> search(@RequestParam String text) {
        log.info("Запрос на поиск вещей: текст={}", text);
        if (text == null || text.isBlank()) {
            log.info("Поиск вещей пропущен: пустой текст запроса");
            return Collections.emptyList();
        }
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                 @PathVariable Long itemId,
                                 @Valid @RequestBody CommentRequestDto commentDto) {
        log.info("Запрос на добавление комментария: userId={}, itemId={}", userId, itemId);
        return itemService.addComment(userId, itemId, commentDto);
    }
}
