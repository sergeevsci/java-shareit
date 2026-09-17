package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private ObjectMapper objectMapper;

    private JacksonTester<ItemRequestDto> json;

    @BeforeEach
    void setUp() {
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    void serializeItemRequestContainsCreatedTimestamp() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(
                1L,
                "Need a drill",
                2L,
                LocalDateTime.of(2026, 9, 15, 10, 0),
                List.of(new RequestedItemDto(3L, "Drill", 4L))
        );

        JsonContent<ItemRequestDto> content = json.write(dto);

        assertThat(content).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(content).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(content).extractingJsonPathStringValue("$.created").isEqualTo("2026-09-15T10:00:00");
        assertThat(content).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(3);
    }

}
