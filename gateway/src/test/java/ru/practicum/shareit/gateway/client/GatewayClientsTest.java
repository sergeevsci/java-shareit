package ru.practicum.shareit.gateway.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.gateway.booking.BookingState;
import ru.practicum.shareit.gateway.booking.dto.BookingRequestDto;
import ru.practicum.shareit.gateway.item.dto.CommentRequestDto;
import ru.practicum.shareit.gateway.user.dto.UserRequestDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GatewayClientsTest {
    private static final String SERVER_URL = "http://localhost:9090";
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private MockRestServiceServer server;
    private UserClient userClient;
    private ItemClient itemClient;
    private BookingClient bookingClient;
    private ItemRequestClient itemRequestClient;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());
        server = MockRestServiceServer.bindTo(restTemplate).build();
        userClient = new UserClient(SERVER_URL, restTemplate);
        itemClient = new ItemClient(SERVER_URL, restTemplate);
        bookingClient = new BookingClient(SERVER_URL, restTemplate);
        itemRequestClient = new ItemRequestClient(SERVER_URL, restTemplate);
    }

    @Test
    void userClientSendsRequests() {
        expect(HttpMethod.POST, "/users", null);
        expect(HttpMethod.PATCH, "/users/1", null);
        expect(HttpMethod.GET, "/users/1", null);
        expect(HttpMethod.GET, "/users", null);
        expect(HttpMethod.DELETE, "/users/1", null);

        assertOk(userClient.createUser(new UserRequestDto("User", "user@example.com")));
        assertOk(userClient.updateUser(1L, new UserRequestDto("Updated", null)));
        assertOk(userClient.getUser(1L));
        assertOk(userClient.getUsers());
        assertOk(userClient.deleteUser(1L));

        server.verify();
    }

    @Test
    void itemClientSendsRequests() {
        ru.practicum.shareit.gateway.item.dto.ItemRequestDto itemDto =
                new ru.practicum.shareit.gateway.item.dto.ItemRequestDto("Drill", "Powerful drill", true, 1L);

        expect(HttpMethod.POST, "/items", 10L);
        expect(HttpMethod.PATCH, "/items/2", 10L);
        expect(HttpMethod.GET, "/items/2", 10L);
        expect(HttpMethod.GET, "/items", 10L);
        expect(HttpMethod.GET, "/items/search?text=drill", null);
        expect(HttpMethod.POST, "/items/2/comment", 10L);

        assertOk(itemClient.createItem(10L, itemDto));
        assertOk(itemClient.updateItem(10L, 2L, itemDto));
        assertOk(itemClient.getItem(10L, 2L));
        assertOk(itemClient.getItemsByOwner(10L));
        assertOk(itemClient.searchItems("drill"));
        assertOk(itemClient.addComment(10L, 2L, new CommentRequestDto("Good")));

        server.verify();
    }

    @Test
    void bookingClientSendsRequests() {
        BookingRequestDto bookingDto = new BookingRequestDto(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                2L
        );

        expect(HttpMethod.POST, "/bookings", 7L);
        expect(HttpMethod.PATCH, "/bookings/3?approved=true", 7L);
        expect(HttpMethod.GET, "/bookings/3", 7L);
        expect(HttpMethod.GET, "/bookings?state=ALL", 7L);
        expect(HttpMethod.GET, "/bookings/owner?state=WAITING", 7L);

        assertOk(bookingClient.createBooking(7L, bookingDto));
        assertOk(bookingClient.approveBooking(7L, 3L, true));
        assertOk(bookingClient.getBooking(7L, 3L));
        assertOk(bookingClient.getBookingsByBooker(7L, BookingState.ALL));
        assertOk(bookingClient.getBookingsByOwner(7L, BookingState.WAITING));

        server.verify();
    }

    @Test
    void itemRequestClientSendsRequests() {
        ru.practicum.shareit.gateway.request.dto.ItemRequestDto requestDto =
                new ru.practicum.shareit.gateway.request.dto.ItemRequestDto("Need a drill");

        expect(HttpMethod.POST, "/requests", 5L);
        expect(HttpMethod.GET, "/requests", 5L);
        expect(HttpMethod.GET, "/requests/all", 5L);
        expect(HttpMethod.GET, "/requests/9", 5L);

        assertOk(itemRequestClient.createRequest(5L, requestDto));
        assertOk(itemRequestClient.getRequests(5L));
        assertOk(itemRequestClient.getAllRequests(5L));
        assertOk(itemRequestClient.getRequest(5L, 9L));

        server.verify();
    }

    private void expect(HttpMethod httpMethod, String path, Long userId) {
        server.expect(once(), requestTo(SERVER_URL + path))
                .andExpect(method(httpMethod))
                .andExpect(request -> {
                    if (userId != null) {
                        header(USER_ID_HEADER, String.valueOf(userId)).match(request);
                    }
                })
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));
    }

    private void assertOk(ResponseEntity<Object> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
