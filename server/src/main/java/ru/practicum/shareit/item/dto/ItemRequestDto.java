package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.Create;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    @NotBlank(groups = Create.class, message = "Название вещи не должно быть пустым")
    private String name;

    @NotBlank(groups = Create.class, message = "Описание вещи не должно быть пустым")
    private String description;

    @NotNull(groups = Create.class, message = "Статус доступности вещи должен быть указан")
    private Boolean available;

    private Long requestId;
}
