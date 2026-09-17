package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto);

    Collection<ItemRequestDto> getByUser(Long userId);

    Collection<ItemRequestDto> getAll(Long userId);

    ItemRequestDto getById(Long userId, Long requestId);
}