package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestedItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIntegrationTest {
    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    void createRequestReturnsSavedRequest() {
        long requestorId = createUser("Requestor", "requestor@example.com");

        long createdId = createRequest(requestorId, "Need a drill");
        ItemRequestDto created = itemRequestService.getById(requestorId, createdId);

        assertNotNull(created.getId());
        assertEquals("Need a drill", created.getDescription());
        assertEquals(requestorId, created.getRequestorId());
        assertNotNull(created.getCreated());
    }

    @Test
    void createRequestByUnknownUserThrowsNotFound() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.create(999_999L, new ItemRequestDto(null, "Need a drill", null, null, null)));
    }

    @Test
    void getByUserReturnsOwnRequests() {
        long requestorId = createUser("Requestor", "requestor@example.com");
        createRequest(requestorId, "First request");
        createRequest(requestorId, "Second request");

        Collection<ItemRequestDto> requests = itemRequestService.getByUser(requestorId);

        assertEquals(2, requests.size());
        assertTrue(requests.stream().anyMatch(request -> "First request".equals(request.getDescription())));
        assertTrue(requests.stream().anyMatch(request -> "Second request".equals(request.getDescription())));
    }

    @Test
    void getAllReturnsOtherUsersRequestsOnly() {
        long firstUserId = createUser("First", "first@example.com");
        long secondUserId = createUser("Second", "second@example.com");
        createRequest(firstUserId, "Request of the first user");

        Collection<ItemRequestDto> requests = itemRequestService.getAll(secondUserId);

        assertEquals(1, requests.size());
        assertEquals("Request of the first user", requests.iterator().next().getDescription());
    }

    @Test
    void getByIdEnrichesRequestWithAnswerItems() {
        long requestorId = createUser("Requestor", "requestor@example.com");
        long ownerId = createUser("Owner", "owner@example.com");
        long requestId = createRequest(requestorId, "Need a drill");
        long itemId = itemService.create(
                ownerId,
                new ru.practicum.shareit.item.dto.ItemRequestDto("Drill", "Powerful drill", true, requestId)
        ).getId();

        ItemRequestDto found = itemRequestService.getById(requestorId, requestId);

        assertFalse(found.getItems().isEmpty());
        RequestedItemDto answered = found.getItems().get(0);
        assertEquals(itemId, answered.getId());
        assertEquals("Drill", answered.getName());
        assertEquals(ownerId, answered.getOwnerId());
    }

    @Test
    void getByIdUnknownRequestThrowsNotFound() {
        long userId = createUser("User", "user@example.com");

        assertThrows(NotFoundException.class, () -> itemRequestService.getById(userId, 999_999L));
    }

    private long createUser(String name, String email) {
        UserDto user = userService.create(new UserRequestDto(name, email));
        return user.getId();
    }

    private long createRequest(long requestorId, String description) {
        return itemRequestService.create(requestorId, new ItemRequestDto(null, description, null, null, null)).getId();
    }
}