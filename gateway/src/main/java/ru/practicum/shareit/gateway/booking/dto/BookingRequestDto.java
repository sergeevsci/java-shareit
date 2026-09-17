package ru.practicum.shareit.gateway.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDto {
    @NotNull(message = "Дата начала бронирования должна быть указана")
    @FutureOrPresent(message = "Дата начала бронирования должна быть в настоящем или будущем")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования должна быть указана")
    @Future(message = "Дата окончания бронирования должна быть в будущем")
    private LocalDateTime end;

    @NotNull(message = "Идентификатор вещи должен быть указан")
    private Long itemId;
}
