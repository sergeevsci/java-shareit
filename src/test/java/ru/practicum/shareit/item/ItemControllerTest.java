package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

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
class ItemControllerTest {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final AtomicLong COUNTER = new AtomicLong();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUpdateGetOwnerItemsAndSearchItem() throws Exception {
        long ownerId = createUser("Owner", uniqueEmail());
        long itemId = createItem(ownerId, "Drill", "Powerful concrete drill", true);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Updated drill description\",\"available\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Math.toIntExact(itemId)))
                .andExpect(jsonPath("$.description").value("Updated drill description"))
                .andExpect(jsonPath("$.available").value(false));

        mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Math.toIntExact(itemId)))
                .andExpect(jsonPath("$.name").value("Drill"));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + itemId + ")]").exists());

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"available\":true}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/items/search")
                        .param("text", "updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + itemId + ")]").exists());
    }

    @Test
    void createItemForUnknownUserReturnsNotFound() throws Exception {
        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 999_999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson("Item", "Description", true)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItemByNotOwnerReturnsNotFound() throws Exception {
        long ownerId = createUser("Owner", uniqueEmail());
        long otherUserId = createUser("Other", uniqueEmail());
        long itemId = createItem(ownerId, "Saw", "Sharp saw", true);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, otherUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New name\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchWithBlankTextReturnsEmptyList() throws Exception {
        long ownerId = createUser("Owner", uniqueEmail());
        createItem(ownerId, "Hammer", "Useful hammer", true);

        mockMvc.perform(get("/items/search")
                        .param("text", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void addCommentAfterFinishedBookingAndGetItemWithComments() throws Exception {
        long ownerId = createUser("Owner", uniqueEmail());
        long bookerId = createUser("Booker", uniqueEmail());
        long itemId = createItem(ownerId, "Laptop", "Work laptop", true);
        addPastApprovedBooking(itemId, bookerId);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Good laptop\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Good laptop"))
                .andExpect(jsonPath("$.authorName").value("Booker"));

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].text").value("Good laptop"))
                .andExpect(jsonPath("$.lastBooking").exists());
    }

    @Test
    void addCommentWithoutFinishedBookingReturnsBadRequest() throws Exception {
        long ownerId = createUser("Owner", uniqueEmail());
        long userId = createUser("User", uniqueEmail());
        long itemId = createItem(ownerId, "Phone", "Mobile phone", true);

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID_HEADER, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Comment\"}"))
                .andExpect(status().isBadRequest());
    }

    private long createUser(String name, String email) throws Exception {
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson(name, email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.email").value(email))
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
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.available").value(available))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractLong(response, "id");
    }

    private String uniqueEmail() {
        return "item-controller-" + COUNTER.incrementAndGet() + "@example.com";
    }

    private String userJson(String name, String email) {
        return "{\"name\":\"" + name + "\",\"email\":\"" + email + "\"}";
    }

    private String itemJson(String name, String description, boolean available) {
        return "{\"name\":\"" + name + "\",\"description\":\"" + description
                + "\",\"available\":" + available + "}";
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
