package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.model.Item;

public final class ItemMapper {
    private ItemMapper() {
    }

    public static ItemDto toDto(Item item) {
        Long requestId = item.getRequest() == null ? null : item.getRequest().getId();
        return new ItemDto(item.getId(), item.getName(), item.getDescription(), item.getAvailable(), requestId);
    }

    public static ItemDto toItemDto(Item item) {
        return toDto(item);
    }

    public static Item toItem(ItemRequestDto itemDto) {
        return new Item(null, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable(), null, null);
    }
}
