package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private static final Sort START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemRequestDto itemDto) {
        User owner = userService.getUser(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        Item savedItem = itemRepository.save(item);
        log.info("Вещь создана: itemId={}, ownerId={}", savedItem.getId(), owner.getId());
        return ItemMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemRequestDto itemDto) {
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
        Item updatedItem = itemRepository.save(item);
        log.info("Вещь обновлена: itemId={}, ownerId={}", updatedItem.getId(), userId);
        return ItemMapper.toDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {
        Item item = getItem(itemId);
        ItemDto itemDto = ItemMapper.toDto(item);
        fillItems(List.of(itemDto), userId != null && item.getOwner().getId().equals(userId));
        log.info("Вещь получена: itemId={}, userId={}", itemId, userId);
        return itemDto;
    }

    @Override
    public Collection<ItemDto> getByOwner(Long userId) {
        userService.getUser(userId);
        List<Item> items = itemRepository.findByOwnerId(userId);
        List<ItemDto> itemDtos = items.stream().map(ItemMapper::toDto).collect(Collectors.toList());
        fillItems(itemDtos, true);
        log.info("Список вещей владельца получен: ownerId={}, найдено={}", userId, itemDtos.size());
        return itemDtos;
    }

    @Override
    public Collection<ItemDto> search(String text) {
        Collection<ItemDto> result = itemRepository.search(text).stream().map(ItemMapper::toDto).collect(Collectors.toList());
        log.info("Поиск вещей завершён: текст={}, найдено={}", text, result.size());
        return result;
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentRequestDto commentDto) {
        User author = userService.getUser(userId);
        Item item = getItem(itemId);
        boolean wasBooked = bookingRepository.existsByItemAndBookerIdAndStatusAndEndBefore(
                item,
                userId,
                BookingStatus.APPROVED,
                LocalDateTime.now()
        );
        if (!wasBooked) {
            throw new ValidationException("Комментарий может оставить только пользователь, завершивший аренду вещи");
        }
        Comment comment = new Comment(null, commentDto.getText(), item, author, LocalDateTime.now());
        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий добавлен: commentId={}, itemId={}, authorId={}", savedComment.getId(), itemId, userId);
        return CommentMapper.toDto(savedComment);
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }

    private void fillItems(List<ItemDto> itemDtos, boolean withBookings) {
        if (itemDtos.isEmpty()) {
            return;
        }
        List<Long> itemIds = itemDtos.stream().map(ItemDto::getId).collect(Collectors.toList());
        Map<Long, List<Comment>> commentsByItem = commentRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        Map<Long, List<Booking>> bookingsByItem = Collections.emptyMap();
        if (withBookings) {
            bookingsByItem = bookingRepository.findByItemIdInAndStatus(itemIds, BookingStatus.APPROVED, START_DESC).stream()
                    .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));
        }
        LocalDateTime now = LocalDateTime.now();
        for (ItemDto itemDto : itemDtos) {
            addComments(itemDto, commentsByItem.getOrDefault(itemDto.getId(), Collections.emptyList()));
            if (!withBookings) {
                continue;
            }
            List<Booking> bookings = bookingsByItem.getOrDefault(itemDto.getId(), Collections.emptyList());
            bookings.stream()
                    .filter(booking -> booking.getEnd().isBefore(now))
                    .max(Comparator.comparing(Booking::getStart))
                    .map(BookingMapper::toShortDto)
                    .ifPresent(itemDto::setLastBooking);
            bookings.stream()
                    .filter(booking -> booking.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .map(BookingMapper::toShortDto)
                    .ifPresent(itemDto::setNextBooking);
        }
    }

    private void addComments(ItemDto itemDto, List<Comment> comments) {
        itemDto.setComments(comments.stream().map(CommentMapper::toDto).collect(Collectors.toList()));
    }

}
