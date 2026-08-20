package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = userService.getUser(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);
        log.info("Вещь создана: itemId={}, ownerId={}", savedItem.getId(), owner.getId());
        return ItemMapper.toDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        Item item = getItem(itemId);
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Редактировать вещь может только владелец");
        }
        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                throw new ValidationException("Название вещи не должно быть пустым");
            }
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                throw new ValidationException("Описание вещи не должно быть пустым");
            }
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        Item updatedItem = itemRepository.update(item);
        log.info("Вещь обновлена: itemId={}, ownerId={}", updatedItem.getId(), userId);
        return ItemMapper.toDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        return ItemMapper.toDto(getItem(itemId));
    }

    @Override
    public Collection<ItemDto> getByOwner(Long userId) {
        userService.getUser(userId);
        return itemRepository.findByOwnerId(userId).stream().map(ItemMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> search(String text) {
        Collection<ItemDto> result = itemRepository.search(text).stream().map(ItemMapper::toDto).collect(Collectors.toList());
        log.info("Поиск вещей завершён: текст={}, найдено={}", text, result.size());
        return result;
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }

}
