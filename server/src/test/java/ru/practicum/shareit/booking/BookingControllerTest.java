package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    private final BookingDto bookingDto = new BookingDto(
            1L,
            LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(2),
            new ItemDto(1L, "Дрель", "Мощная дрель", true, null, null, null, Collections.emptyList()),
            new UserDto(1L, "User", "user@example.com"),
            BookingStatus.WAITING
    );

    @Test
    void createBookingReturnsBooking() throws Exception {
        when(bookingService.create(any(Long.class), any(BookingRequestDto.class))).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\":\"2026-09-20T10:00:00\",\"end\":\"2026-09-21T10:00:00\",\"itemId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void createBookingWithoutUserIdHeaderReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\":\"2026-09-20T10:00:00\",\"end\":\"2026-09-21T10:00:00\",\"itemId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBookingReturnsBooking() throws Exception {
        when(bookingService.approve(any(Long.class), any(Long.class), any(Boolean.class)))
                .thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getByIdReturnsBooking() throws Exception {
        when(bookingService.getById(any(Long.class), any(Long.class))).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1L).header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByBookerReturnsBookings() throws Exception {
        when(bookingService.getByBooker(any(Long.class), any(BookingState.class))).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getByBookerWithUnknownStateReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 3L)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByOwnerReturnsBookings() throws Exception {
        when(bookingService.getByOwner(any(Long.class), any(BookingState.class))).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }
}
