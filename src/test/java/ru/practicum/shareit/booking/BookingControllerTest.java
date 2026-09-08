package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
class BookingControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final AtomicLong COUNTER = new AtomicLong();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createApproveGetAndListBooking() throws Exception {
        long ownerId = createUser("Owner", uniqueEmail());
        long bookerId = createUser("Booker", uniqueEmail());
        long itemId = createItem(ownerId, "Camera", "Digital camera", true);
        long bookingId = createBooking(bookerId, itemId, 1, 2);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, ownerId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Math.toIntExact(bookingId)))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, bookerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Math.toIntExact(bookingId)))
                .andExpect(jsonPath("$.booker.id").value(Math.toIntExact(bookerId)))
                .andExpect(jsonPath("$.item.id").value(Math.toIntExact(itemId)));

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + bookingId + ")]").exists());

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_ID_HEADER, ownerId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + bookingId + ")]").exists());
    }

    @Test
    void createBookingByOwnerReturnsNotFound() throws Exception {
        long ownerId = createUser("Owner", uniqueEmail());
        long itemId = createItem(ownerId, "Tent", "Camping tent", true);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(itemId, 1, 2)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBookingForUnavailableItemReturnsBadRequest() throws Exception {
        long ownerId = createUser("Owner", uniqueEmail());
        long bookerId = createUser("Booker", uniqueEmail());
        long itemId = createItem(ownerId, "Bike", "City bike", false);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(itemId, 1, 2)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOverlappingBookingReturnsBadRequest() throws Exception {
        long ownerId = createUser("Owner", uniqueEmail());
        long firstBookerId = createUser("First Booker", uniqueEmail());
        long secondBookerId = createUser("Second Booker", uniqueEmail());
        long itemId = createItem(ownerId, "Printer", "Color printer", true);

        createBooking(firstBookerId, itemId, 1, 4);

        mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, secondBookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(itemId, 2, 3)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBookingByNotOwnerReturnsForbidden() throws Exception {
        long ownerId = createUser("Owner", uniqueEmail());
        long bookerId = createUser("Booker", uniqueEmail());
        long otherUserId = createUser("Other", uniqueEmail());
        long itemId = createItem(ownerId, "Speaker", "Portable speaker", true);
        long bookingId = createBooking(bookerId, itemId, 1, 2);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(USER_ID_HEADER, otherUserId)
                        .param("approved", "true"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBookingsWithUnknownStateReturnsBadRequest() throws Exception {
        long userId = createUser("User", uniqueEmail());

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, userId)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFutureBookingsReturnsOnlyFutureBookings() throws Exception {
        long ownerId = createUser("Owner", uniqueEmail());
        long bookerId = createUser("Booker", uniqueEmail());
        long itemId = createItem(ownerId, "Projector", "Office projector", true);
        long bookingId = createBooking(bookerId, itemId, 3, 4);

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + bookingId + ")]").exists());

        mockMvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .param("state", "PAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private long createUser(String name, String email) throws Exception {
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(name, email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractLong(response, "id");
    }

    private long createItem(long ownerId, String name, String description, boolean available) throws Exception {
        String response = mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson(name, description, available)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractLong(response, "id");
    }

    private long createBooking(long bookerId, long itemId, int startDays, int endDays) throws Exception {
        String response = mockMvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingJson(itemId, startDays, endDays)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractLong(response, "id");
    }

    private String uniqueEmail() {
        return "booking-controller-" + COUNTER.incrementAndGet() + "@example.com";
    }

    private String userJson(String name, String email) {
        return "{\"name\":\"" + name + "\",\"email\":\"" + email + "\"}";
    }

    private String itemJson(String name, String description, boolean available) {
        return "{\"name\":\"" + name + "\",\"description\":\"" + description
                + "\",\"available\":" + available + "}";
    }

    private String bookingJson(long itemId, int startDays, int endDays) {
        return "{\"itemId\":" + itemId + ",\"start\":\"" + LocalDateTime.now().plusDays(startDays)
                + "\",\"end\":\"" + LocalDateTime.now().plusDays(endDays) + "\"}";
    }

    private long extractLong(String json, String fieldName) {
        String field = "\"" + fieldName + "\":";
        int start = json.indexOf(field) + field.length();
        int end = json.indexOf(',', start);
        if (end == -1) {
            end = json.indexOf('}', start);
        }
        return Long.parseLong(json.substring(start, end).trim());
    }
}
