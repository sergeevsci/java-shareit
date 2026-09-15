package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        User requestor = userService.getUser(userId);
        ItemRequest itemRequest = new ItemRequest(
                null,
                itemRequestDto.getDescription(),
                requestor,
                LocalDateTime.now()
        );
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Запрос вещи создан: requestId={}, requestorId={}", savedRequest.getId(), userId);
        return ItemRequestMapper.toDto(savedRequest);
    }

    @Override
    public Collection<ItemRequestDto> getByUser(Long userId) {
        userService.getUser(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        Collection<ItemRequestDto> result = fillItems(requests);
        log.info("Список запросов пользователя получен: requestorId={}, найдено={}", userId, result.size());
        return result;
    }

    @Override
    public Collection<ItemRequestDto> getAll(Long userId) {
        userService.getUser(userId);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(userId);
        Collection<ItemRequestDto> result = fillItems(requests);
        log.info("Список запросов других пользователей получен: userId={}, найдено={}", userId, result.size());
        return result;
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        userService.getUser(userId);
        ItemRequest itemRequest = getRequest(requestId);
        List<Item> items = itemRepository.findByRequestId(requestId);
        log.info("Запрос вещи получен: requestId={}, userId={}", requestId, userId);
        return ItemRequestMapper.toDto(itemRequest, items);
    }

    private Collection<ItemRequestDto> fillItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());
        Map<Long, List<Item>> itemsByRequest = itemRepository.findByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return requests.stream()
                .map(request -> ItemRequestMapper.toDto(
                        request,
                        itemsByRequest.getOrDefault(request.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    private ItemRequest getRequest(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос вещи с id " + requestId + " не найден"));
    }
}