package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserRequestDto;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {
    private static final String EMAIL = "user@example.com";

    @Autowired
    private UserService userService;

    @Test
    void createUserReturnsSavedUser() {
        UserDto created = userService.create(new UserRequestDto("User", EMAIL));

        assertNotNull(created.getId());
        assertEquals("User", created.getName());
        assertEquals(EMAIL, created.getEmail());
    }

    @Test
    void createUserWithDuplicateEmailThrowsConflict() {
        userService.create(new UserRequestDto("First", EMAIL));

        assertThrows(ConflictException.class,
                () -> userService.create(new UserRequestDto("Second", EMAIL)));
    }

    @Test
    void updateUserChangesOnlyProvidedFields() {
        UserDto created = userService.create(new UserRequestDto("User", EMAIL));

        UserDto updated = userService.update(created.getId(), new UserRequestDto("Updated", null));

        assertEquals("Updated", updated.getName());
        assertEquals(EMAIL, updated.getEmail());
    }

    @Test
    void getByIdReturnsUserOrThrowsNotFound() {
        UserDto created = userService.create(new UserRequestDto("User", EMAIL));

        assertEquals(created.getId(), userService.getById(created.getId()).getId());
        assertThrows(NotFoundException.class, () -> userService.getById(999_999L));
    }

    @Test
    void getAllReturnsAllUsers() {
        userService.create(new UserRequestDto("First", "first@example.com"));
        userService.create(new UserRequestDto("Second", "second@example.com"));

        Collection<UserDto> all = userService.getAll();

        assertEquals(2, all.size());
    }

    @Test
    void deleteRemovesUser() {
        UserDto created = userService.create(new UserRequestDto("User", EMAIL));

        userService.delete(created.getId());

        assertThrows(NotFoundException.class, () -> userService.getById(created.getId()));
    }
}