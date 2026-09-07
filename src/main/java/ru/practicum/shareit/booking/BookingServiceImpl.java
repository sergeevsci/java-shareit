package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private static final Sort START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingRequestDto bookingDto) {
        User booker = userService.getUser(userId);
        Item item = getItem(bookingDto.getItemId());
        validateBookingDates(bookingDto);
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        Booking booking = new Booking(
                null,
                bookingDto.getStart(),
                bookingDto.getEnd(),
                item,
                booker,
                BookingStatus.WAITING
        );
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование создано: bookingId={}, bookerId={}, itemId={}", savedBooking.getId(), userId, item.getId());
        return BookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        if (approved == null) {
            throw new ValidationException("Решение по бронированию должно быть указано");
        }
        Booking booking = getBooking(bookingId);
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Подтверждать бронирование может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования уже был изменён");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Статус бронирования изменён: bookingId={}, status={}", bookingId, savedBooking.getStatus());
        return BookingMapper.toDto(savedBooking);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        userService.getUser(userId);
        Booking booking = getBooking(bookingId);
        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        if (!ownerId.equals(userId) && !bookerId.equals(userId)) {
            throw new NotFoundException("Бронирование недоступно для пользователя");
        }
        log.info("Бронирование получено: bookingId={}, userId={}", bookingId, userId);
        return BookingMapper.toDto(booking);
    }

    @Override
    public Collection<BookingDto> getByBooker(Long userId, BookingState state) {
        userService.getUser(userId);
        Collection<BookingDto> bookings = findByBooker(userId, state).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
        log.info("Список бронирований пользователя получен: userId={}, state={}, найдено={}", userId, state, bookings.size());
        return bookings;
    }

    @Override
    public Collection<BookingDto> getByOwner(Long userId, BookingState state) {
        userService.getUser(userId);
        Collection<BookingDto> bookings = findByOwner(userId, state).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
        log.info("Список бронирований владельца получен: userId={}, state={}, найдено={}", userId, state, bookings.size());
        return bookings;
    }

    private Collection<Booking> findByBooker(Long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case ALL -> bookingRepository.findByBookerId(userId, START_DESC);
            case CURRENT -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(userId, now, now, START_DESC);
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(userId, now, START_DESC);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(userId, now, START_DESC);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, START_DESC);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, START_DESC);
        };
    }

    private Collection<Booking> findByOwner(Long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case ALL -> bookingRepository.findByItemOwnerId(userId, START_DESC);
            case CURRENT -> bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(userId, now, now, START_DESC);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBefore(userId, now, START_DESC);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfter(userId, now, START_DESC);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, START_DESC);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, START_DESC);
        };
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));
    }

    private Booking getBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id " + bookingId + " не найдено"));
    }

    private void validateBookingDates(BookingRequestDto bookingDto) {
        if (!bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            throw new ValidationException("Дата окончания бронирования должна быть позже даты начала");
        }
    }
}
