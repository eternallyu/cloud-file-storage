package ru.eternallyu.cloudfilestorage.integration.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.eternallyu.cloudfilestorage.integration.IntegrationTestBase;
import ru.eternallyu.cloudfilestorage.service.UserService;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTest extends IntegrationTestBase {

    private static final String TEST_USERNAME = "username";

    private static final String TEST_PASSWORD = "password";

    private static final String NON_EXISTENT_USERNAME = "non-existent-username";

    @Autowired
    private UserService userService;

    @Test
    void testFindUser_UserNotFound() {
        assertThrows(UsernameNotFoundException.class, () -> userService.findByUsername(NON_EXISTENT_USERNAME));
    }
}
