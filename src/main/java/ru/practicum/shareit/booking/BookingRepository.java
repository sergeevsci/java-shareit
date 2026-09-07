package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(Long bookerId,
                                                          LocalDateTime start,
                                                          LocalDateTime end,
                                                          Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(Long ownerId,
                                                             LocalDateTime start,
                                                             LocalDateTime end,
                                                             Sort sort);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    List<Booking> findByItemIdAndStatusAndEndBefore(Long itemId,
                                                    BookingStatus status,
                                                    LocalDateTime end,
                                                    Sort sort);

    List<Booking> findByItemIdAndStatusAndStartAfter(Long itemId,
                                                      BookingStatus status,
                                                      LocalDateTime start,
                                                      Sort sort);

    boolean existsByItemAndBookerIdAndStatusAndEndBefore(Item item,
                                                         Long bookerId,
                                                         BookingStatus status,
                                                         LocalDateTime end);

    List<Booking> findByItemIdInAndStatus(Collection<Long> itemIds, BookingStatus status, Sort sort);
}
