package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class ShareItTests {
    private static final AtomicLong COUNTER = new AtomicLong();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void createUpdateGetAndDeleteUser() throws Exception {
        String email = uniqueEmail();
        long userId = createUser("User", email);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated user\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Math.toIntExact(userId)))
                .andExpect(jsonPath("$.name").value("Updated user"))
                .andExpect(jsonPath("$.email").value(email));

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Math.toIntExact(userId)))
                .andExpect(jsonPath("$.name").value("Updated user"));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + userId + ")]").exists());

        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUserWithDuplicateEmailReturnsConflict() throws Exception {
        String email = uniqueEmail();
        createUser("User", email);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Duplicate", email)))
                .andExpect(status().isConflict());
    }

    @Test
    void createUserWithInvalidEmailReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("User", "invalid-email")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUpdateGetOwnerItemsAndSearchItem() throws Exception {
        long ownerId = createUser("Owner", uniqueEmail());
        long itemId = createItem(ownerId, "Drill", "Powerful concrete drill", true);

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", ownerId)
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
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + itemId + ")]").exists());

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", ownerId)
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
                        .header("X-Sharer-User-Id", 999_999)
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
                        .header("X-Sharer-User-Id", otherUserId)
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
                        .header("X-Sharer-User-Id", ownerId)
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
        return "user" + COUNTER.incrementAndGet() + "@example.com";
    }

    private String userJson(String name, String email) {
        return "{\"name\":\"" + name + "\",\"email\":\"" + email + "\"}";
    }

    private String itemJson(String name, String description, boolean available) {
        return "{\"name\":\"" + name + "\",\"description\":\"" + description
                + "\",\"available\":" + available + "}";
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
