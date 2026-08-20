package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.util.Collection;

public interface UserService {
    UserDto create(UserRequestDto userDto);

    UserDto update(Long userId, UserRequestDto userDto);

    UserDto getById(Long userId);

    Collection<UserDto> getAll();

    void delete(Long userId);

    User getUser(Long userId);
}
