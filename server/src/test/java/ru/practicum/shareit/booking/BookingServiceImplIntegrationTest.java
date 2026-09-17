package ru.practicum.shareit.booking;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void createBookingReturnsWaitingBooking() {
        long ownerId = createUser("Owner", "owner@example.com");
        long bookerId = createUser("Booker", "booker@example.com");
        long itemId = createItem(ownerId, "Camera", "Digital camera", true);

        BookingDto booking = bookingService.create(bookerId, bookingRequest(itemId, 1, 2));

        assertNotNull(booking.getId());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
        assertEquals(bookerId, booking.getBooker().getId());
    }

    @Test
    void createBookingByOwnerThrowsNotFound() {
        long ownerId = createUser("Owner", "owner@example.com");
        long itemId = createItem(ownerId, "Camera", "Digital camera", true);

        assertThrows(NotFoundException.class,
                () -> bookingService.create(ownerId, bookingRequest(itemId, 1, 2)));
    }

    @Test
    void createBookingForUnavailableItemThrowsValidation() {
        long ownerId = createUser("Owner", "owner@example.com");
        long bookerId = createUser("Booker", "booker@example.com");
        long itemId = createItem(ownerId, "Bike", "City bike", false);

        assertThrows(ValidationException.class,
                () -> bookingService.create(bookerId, bookingRequest(itemId, 1, 2)));
    }

    @Test
    void createBookingWithReversedDatesThrowsValidation() {
        long ownerId = createUser("Owner", "owner@example.com");
        long bookerId = createUser("Booker", "booker@example.com");
        long itemId = createItem(ownerId, "Camera", "Digital camera", true);

        assertThrows(ValidationException.class,
                () -> bookingService.create(bookerId, bookingRequest(itemId, 2, 1)));
    }

    @Test
    void approveBookingByOwnerChangesStatus() {
        long ownerId = createUser("Owner", "owner@example.com");
        long bookerId = createUser("Booker", "booker@example.com");
        long itemId = createItem(ownerId, "Camera", "Digital camera", true);
        BookingDto booking = bookingService.create(bookerId, bookingRequest(itemId, 1, 2));

        BookingDto approved = bookingService.approve(ownerId, booking.getId(), true);

        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void approveBookingByNotOwnerThrowsAccessDenied() {
        long ownerId = createUser("Owner", "owner@example.com");
        long otherId = createUser("Other", "other@example.com");
        long bookerId = createUser("Booker", "booker@example.com");
        long itemId = createItem(ownerId, "Camera", "Digital camera", true);
        BookingDto booking = bookingService.create(bookerId, bookingRequest(itemId, 1, 2));

        assertThrows(AccessDeniedException.class,
                () -> bookingService.approve(otherId, booking.getId(), true));
    }

    @Test
    void getByIdIsAvailableForOwnerAndBooker() {
        long ownerId = createUser("Owner", "owner@example.com");
        long bookerId = createUser("Booker", "booker@example.com");
        long itemId = createItem(ownerId, "Camera", "Digital camera", true);
        BookingDto booking = bookingService.create(bookerId, bookingRequest(itemId, 1, 2));

        assertEquals(booking.getId(), bookingService.getById(ownerId, booking.getId()).getId());
        assertEquals(booking.getId(), bookingService.getById(bookerId, booking.getId()).getId());
    }

    @Test
    void getByBookerFiltersFutureBookings() {
        long ownerId = createUser("Owner", "owner@example.com");
        long bookerId = createUser("Booker", "booker@example.com");
        long itemId = createItem(ownerId, "Camera", "Digital camera", true);
        BookingDto future = bookingService.create(bookerId, bookingRequest(itemId, 3, 4));

        Collection<BookingDto> futureBookings = bookingService.getByBooker(bookerId, BookingState.FUTURE);
        Collection<BookingDto> pastBookings = bookingService.getByBooker(bookerId, BookingState.PAST);

        assertTrue(futureBookings.stream().anyMatch(booking -> booking.getId().equals(future.getId())));
        assertTrue(pastBookings.isEmpty());
    }

    @Test
    void getByOwnerReturnsOwnerItemBookings() {
        long ownerId = createUser("Owner", "owner@example.com");
        long bookerId = createUser("Booker", "booker@example.com");
        long itemId = createItem(ownerId, "Camera", "Digital camera", true);
        BookingDto booking = bookingService.create(bookerId, bookingRequest(itemId, 1, 2));

        Collection<BookingDto> ownerBookings = bookingService.getByOwner(ownerId, BookingState.ALL);

        assertTrue(ownerBookings.stream().anyMatch(item -> item.getId().equals(booking.getId())));
    }

    private long createUser(String name, String email) {
        User user = new User(null, name, email);
        entityManager.persist(user);
        return user.getId();
    }

    private long createItem(long ownerId, String name, String description, boolean available) {
        User owner = entityManager.find(User.class, ownerId);
        Item item = new Item(null, name, description, available, owner, null);
        entityManager.persist(item);
        return item.getId();
    }

    private BookingRequestDto bookingRequest(long itemId, int startDays, int endDays) {
        return new BookingRequestDto(
                LocalDateTime.now().plusDays(startDays),
                LocalDateTime.now().plusDays(endDays),
                itemId
        );
    }
}
