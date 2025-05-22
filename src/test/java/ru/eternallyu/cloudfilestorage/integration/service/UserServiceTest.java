package ru.eternallyu.cloudfilestorage.integration.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.eternallyu.cloudfilestorage.dto.request.UserRequestDto;
import ru.eternallyu.cloudfilestorage.dto.response.UserResponseDto;
import ru.eternallyu.cloudfilestorage.error.UserAlreadyExistsException;
import ru.eternallyu.cloudfilestorage.integration.IntegrationTestBase;
import ru.eternallyu.cloudfilestorage.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTest extends IntegrationTestBase {

    private static final String TEST_USERNAME = "username";

    private static final String TEST_PASSWORD = "password";

    private static final String NON_EXISTENT_USERNAME = "non-existent-username";

    @Autowired
    private UserService userService;

    @Test
    void testCreateUser_Success() {
        UserRequestDto userRequestDto = new UserRequestDto(TEST_USERNAME,
                TEST_PASSWORD);

        userService.saveUser(userRequestDto);

        UserResponseDto userResponseDto = userService.findByUsername(TEST_USERNAME);

        assertThat(userResponseDto).isNotNull();
        assertThat(userResponseDto.getUsername()).isEqualTo(TEST_USERNAME);
    }

    @Test
    void testCreateUser_UserAlreadyExist() {
        UserRequestDto userRequestDto = new UserRequestDto(TEST_USERNAME,
                TEST_PASSWORD);

        userService.saveUser(userRequestDto);

        assertThrows(UserAlreadyExistsException.class, () -> userService.saveUser(userRequestDto));
    }

    @Test
    void testFindUser_UserNotFound() {
        assertThrows(UsernameNotFoundException.class, () -> userService.findByUsername(NON_EXISTENT_USERNAME));
    }
}
