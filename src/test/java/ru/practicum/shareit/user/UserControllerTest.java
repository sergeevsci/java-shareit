package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {
    private static final AtomicLong COUNTER = new AtomicLong();

    @Autowired
    private MockMvc mockMvc;

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

    private String uniqueEmail() {
        return "user-controller-" + COUNTER.incrementAndGet() + "@example.com";
    }

    private String userJson(String name, String email) {
        return "{\"name\":\"" + name + "\",\"email\":\"" + email + "\"}";
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
