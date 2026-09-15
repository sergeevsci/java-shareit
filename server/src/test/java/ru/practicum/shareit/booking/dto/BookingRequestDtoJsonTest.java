package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@JsonTest
class BookingRequestDtoJsonTest {
    @Autowired
    private ObjectMapper objectMapper;

    private JacksonTester<BookingRequestDto> json;

    @BeforeEach
    void setUp() {
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    void deserializeBookingRequestFromJson() throws Exception {
        String content = "{\"start\":\"2026-09-20T10:00:00\",\"end\":\"2026-09-21T10:00:00\",\"itemId\":5}";

        BookingRequestDto dto = json.parse(content).getObject();

        assertNotNull(dto);
        assertEquals(LocalDateTime.of(2026, 9, 20, 10, 0), dto.getStart());
        assertEquals(LocalDateTime.of(2026, 9, 21, 10, 0), dto.getEnd());
        assertEquals(5L, dto.getItemId());
    }

    @Test
    void serializeBookingRequestToJson() throws Exception {
        BookingRequestDto dto = new BookingRequestDto(
                LocalDateTime.of(2026, 9, 20, 10, 0),
                LocalDateTime.of(2026, 9, 21, 10, 0),
                5L
        );

        JsonContent<BookingRequestDto> content = json.write(dto);

        assertThat(content).extractingJsonPathStringValue("$.start").isEqualTo("2026-09-20T10:00:00");
        assertThat(content).extractingJsonPathNumberValue("$.itemId").isEqualTo(5);
    }

    @Test
    void missingRequiredFieldsFailValidation() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Set<ConstraintViolation<BookingRequestDto>> violations = validator.validate(new BookingRequestDto());

        assertEquals(3, violations.size());
    }
}