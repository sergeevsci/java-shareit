package ru.practicum.shareit.gateway.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.gateway.booking.BookingState;
import ru.practicum.shareit.gateway.booking.dto.BookingRequestDto;

import java.util.Map;

public class BookingClient extends BaseClient {
    public BookingClient(String serverUrl, RestTemplate rest) {
        super(serverUrl, rest);
    }

    public ResponseEntity<Object> createBooking(long userId, BookingRequestDto bookingDto) {
        return post("/bookings", userId, bookingDto);
    }

    public ResponseEntity<Object> approveBooking(long userId, long bookingId, Boolean approved) {
        return patch("/bookings/" + bookingId, userId, Map.of("approved", approved), null);
    }

    public ResponseEntity<Object> getBooking(long userId, long bookingId) {
        return get("/bookings/" + bookingId, userId);
    }

    public ResponseEntity<Object> getBookingsByBooker(long userId, BookingState state) {
        return get("/bookings", userId, Map.of("state", state));
    }

    public ResponseEntity<Object> getBookingsByOwner(long userId, BookingState state) {
        return get("/bookings/owner", userId, Map.of("state", state));
    }
}