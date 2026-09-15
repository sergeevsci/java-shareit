package ru.practicum.shareit.gateway.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.booking.dto.BookingRequestDto;
import ru.practicum.shareit.gateway.client.BookingClient;

import java.util.List;
import java.util.Map;

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
    private BookingClient bookingClient;

    @Test
    void createBookingReturnsBooking() throws Exception {
        when(bookingClient.createBooking(any(Long.class), any(BookingRequestDto.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "status", "WAITING")));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\":\"2026-09-20T10:00:00\",\"end\":\"2026-09-21T10:00:00\",\"itemId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void createBookingWithoutEndDateReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"start\":\"2026-09-20T10:00:00\",\"itemId\":1}"))
                .andExpect(status().isBadRequest());
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
        when(bookingClient.approveBooking(any(Long.class), any(Long.class), any(Boolean.class)))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "status", "APPROVED")));

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getByIdReturnsBooking() throws Exception {
        when(bookingClient.getBooking(1L, 1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1, "status", "WAITING")));

        mockMvc.perform(get("/bookings/{bookingId}", 1L).header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByBookerReturnsBookings() throws Exception {
        when(bookingClient.getBookingsByBooker(any(Long.class), any(BookingState.class)))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1, "status", "WAITING"))));

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
        when(bookingClient.getBookingsByOwner(any(Long.class), any(BookingState.class)))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1, "status", "WAITING"))));

        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }
}