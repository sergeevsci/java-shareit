package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto create(UserDto userDto) {
        validateNewUser(userDto);
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Email already exists");
        }
        User savedUser = userRepository.save(UserMapper.toUser(userDto));
        log.info("User created: userId={}", savedUser.getId());
        return UserMapper.toDto(savedUser);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User user = getUser(userId);
        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                throw new ValidationException("User name must not be blank");
            }
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());
            if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), userId)) {
                throw new ConflictException("Email already exists");
            }
            user.setEmail(userDto.getEmail());
        }
        User updatedUser = userRepository.update(user);
        log.info("User updated: userId={}", updatedUser.getId());
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
        log.info("User deleted: userId={}", userId);
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }

    private void validateNewUser(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("User name must not be blank");
        }
        if (userDto.getEmail() == null) {
            throw new ValidationException("Email must not be null");
        }
        validateEmail(userDto.getEmail());
    }

    private void validateEmail(String email) {
        if (email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Email is invalid");
        }
    }
}
