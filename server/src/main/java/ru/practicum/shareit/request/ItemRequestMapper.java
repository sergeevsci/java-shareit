package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestedItemDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ItemRequestMapper {
    private ItemRequestMapper() {
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User requestor) {
        return new ItemRequest(
                null,
                itemRequestDto.getDescription(),
                requestor,
                LocalDateTime.now()
        );
    }

    public static ItemRequestDto toDto(ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getRequestor().getId(),
                itemRequest.getCreated(),
                Collections.emptyList()
        );
    }

    public static ItemRequestDto toDto(ItemRequest itemRequest, List<Item> items) {
        List<RequestedItemDto> itemDtos = items.stream()
                .map(ItemRequestMapper::toRequestedItemDto)
                .collect(Collectors.toList());
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getRequestor().getId(),
                itemRequest.getCreated(),
                itemDtos
        );
    }

    private static RequestedItemDto toRequestedItemDto(Item item) {
        return new RequestedItemDto(item.getId(), item.getName(), item.getOwner().getId());
    }
}
