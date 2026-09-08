package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(UserRequestDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
        User savedUser = userRepository.save(UserMapper.toUser(userDto));
        log.info("Пользователь создан: userId={}", savedUser.getId());
        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto update(Long userId, UserRequestDto userDto) {
        User user = getUser(userId);
        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                throw new ValidationException("Имя пользователя не должно быть пустым");
            }
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());
            if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
                throw new ConflictException("Пользователь с таким email уже существует");
            }
            user.setEmail(userDto.getEmail());
        }
        User updatedUser = userRepository.save(user);
        log.info("Пользователь обновлён: userId={}", updatedUser.getId());
        return UserMapper.toDto(updatedUser);
    }

    @Override
    public UserDto getById(Long userId) {
        return UserMapper.toDto(getUser(userId));
    }

    @Override
    public Collection<UserDto> getAll() {
        return userRepository.findAll().stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) {
        userRepository.deleteById(userId);
        log.info("Пользователь удалён: userId={}", userId);
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    private void validateEmail(String email) {
        if (email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Некорректный email");
        }
    }
}
