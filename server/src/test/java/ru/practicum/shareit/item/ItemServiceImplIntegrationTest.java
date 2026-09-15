package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
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
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createItemReturnsItemWithOwner() {
        UserDto owner = createUser("Owner", "owner@example.com");

        ItemDto created = itemService.create(owner.getId(), itemDto("Drill", "Powerful drill", true));

        assertNotNull(created.getId());
        assertEquals("Drill", created.getName());
        assertEquals("Powerful drill", created.getDescription());
        assertEquals(true, created.getAvailable());
    }

    @Test
    void createItemByUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class,
                () -> itemService.create(999_999L, itemDto("Drill", "Powerful drill", true)));
    }

    @Test
    void updateItemChangesProvidedFields() {
        UserDto owner = createUser("Owner", "owner@example.com");
        long itemId = createItem(owner.getId(), "Drill", "Powerful drill", true);

        ItemDto updated = itemService.update(owner.getId(), itemId, itemDto("Saw", null, false));

        assertEquals("Saw", updated.getName());
        assertEquals("Powerful drill", updated.getDescription());
        assertEquals(false, updated.getAvailable());
    }

    @Test
    void updateItemByNotOwnerThrowsNotFound() {
        UserDto owner = createUser("Owner", "owner@example.com");
        UserDto other = createUser("Other", "other@example.com");
        long itemId = createItem(owner.getId(), "Drill", "Powerful drill", true);

        assertThrows(NotFoundException.class,
                () -> itemService.update(other.getId(), itemId, itemDto("New", "New desc", true)));
    }

    @Test
    void getByIdFillsLastBookingForOwner() {
        UserDto owner = createUser("Owner", "owner@example.com");
        UserDto booker = createUser("Booker", "booker@example.com");
        long itemId = createItem(owner.getId(), "Camera", "Digital camera", true);
        addPastApprovedBooking(itemId, booker.getId());

        ItemDto forOwner = itemService.getById(owner.getId(), itemId);
        ItemDto forBooker = itemService.getById(booker.getId(), itemId);

        assertNotNull(forOwner.getLastBooking());
        assertNull(forBooker.getLastBooking());
    }

    @Test
    void getByOwnerReturnsOnlyOwnerItems() {
        UserDto owner = createUser("Owner", "owner@example.com");
        UserDto other = createUser("Other", "other@example.com");
        long firstItem = createItem(owner.getId(), "Drill", "Powerful drill", true);
        long secondItem = createItem(owner.getId(), "Saw", "Sharp saw", true);
        createItem(other.getId(), "Hammer", "Useful hammer", true);

        Collection<ItemDto> ownerItems = itemService.getByOwner(owner.getId());

        assertEquals(2, ownerItems.size());
        assertTrue(ownerItems.stream().anyMatch(item -> item.getId() == firstItem));
        assertTrue(ownerItems.stream().anyMatch(item -> item.getId() == secondItem));
    }

    @Test
    void searchFindsAvailableItemByTextIgnoreCase() {
        UserDto owner = createUser("Owner", "owner@example.com");
        long available = createItem(owner.getId(), "Drill", "Powerful drill", true);
        createItem(owner.getId(), "Drill Pro", "Advanced drill", false);

        Collection<ItemDto> found = itemService.search("DRILL");

        assertEquals(1, found.size());
        assertEquals(available, found.iterator().next().getId());
    }

    @Test
    void searchWithUnknownTextReturnsEmptyCollection() {
        UserDto owner = createUser("Owner", "owner@example.com");
        createItem(owner.getId(), "Drill", "Powerful drill", true);

        Collection<ItemDto> found = itemService.search("unknown");

        assertTrue(found.isEmpty());
    }

    @Test
    void addCommentAfterFinishedBookingIsStored() {
        UserDto owner = createUser("Owner", "owner@example.com");
        UserDto booker = createUser("Booker", "booker@example.com");
        long itemId = createItem(owner.getId(), "Laptop", "Work laptop", true);
        addPastApprovedBooking(itemId, booker.getId());

        CommentDto comment = itemService.addComment(
                booker.getId(),
                itemId,
                new CommentRequestDto("Good laptop")
        );

        assertEquals("Good laptop", comment.getText());
        assertEquals("Booker", comment.getAuthorName());
        assertNotNull(comment.getCreated());
    }

    @Test
    void addCommentWithoutFinishedBookingThrowsValidation() {
        UserDto owner = createUser("Owner", "owner@example.com");
        UserDto user = createUser("User", "user@example.com");
        long itemId = createItem(owner.getId(), "Phone", "Mobile phone", true);

        assertThrows(ValidationException.class,
                () -> itemService.addComment(user.getId(), itemId, new CommentRequestDto("Comment")));
    }

    private UserDto createUser(String name, String email) {
        return userService.create(new UserRequestDto(name, email));
    }

    private long createItem(long ownerId, String name, String description, boolean available) {
        return itemService.create(ownerId, itemDto(name, description, available)).getId();
    }

    private ItemRequestDto itemDto(String name, String description, boolean available) {
        return new ItemRequestDto(name, description, available, null);
    }

    private void addPastApprovedBooking(long itemId, long bookerId) {
        Item item = itemRepository.findById(itemId).orElseThrow();
        User booker = userRepository.findById(bookerId).orElseThrow();
        Booking booking = new Booking(
                null,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item,
                booker,
                BookingStatus.APPROVED
        );
        bookingRepository.save(booking);
    }
}