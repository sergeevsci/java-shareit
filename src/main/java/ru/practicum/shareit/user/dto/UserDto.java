package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.Create;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    @NotBlank(groups = Create.class, message = "Имя пользователя не должно быть пустым")
    private String name;

    @NotBlank(groups = Create.class, message = "Email не должен быть пустым")
    @Email(groups = Create.class, message = "Некорректный email")
    private String email;
}
