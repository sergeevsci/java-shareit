package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;

import java.util.Collection;

public interface ItemService {
    ItemDto create(Long userId, ItemRequestDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemRequestDto itemDto);

    ItemDto getById(Long userId, Long itemId);

    Collection<ItemDto> getByOwner(Long userId);

    Collection<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentRequestDto commentDto);
}
