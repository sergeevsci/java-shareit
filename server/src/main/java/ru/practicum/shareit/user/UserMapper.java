package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRequestDto;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }

    public static UserDto toUserDto(User user) {
        return toDto(user);
    }

    public static User toUser(UserRequestDto userDto) {
        return new User(null, userDto.getName(), userDto.getEmail());
    }
}
