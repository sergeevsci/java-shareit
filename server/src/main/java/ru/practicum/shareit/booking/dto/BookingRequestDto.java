package ru.practicum.shareit.booking.dto;

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
    private LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования должна быть указана")
    private LocalDateTime end;

    @NotNull(message = "Идентификатор вещи должен быть указан")
    private Long itemId;
}
