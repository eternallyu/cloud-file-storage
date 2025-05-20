package ru.eternallyu.cloudfilestorage.http.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.eternallyu.cloudfilestorage.dto.response.UserResponseDto;
import ru.eternallyu.cloudfilestorage.security.CustomUserDetails;
import ru.eternallyu.cloudfilestorage.service.UserService;

@RestController
@RequestMapping("/api/user/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponseDto> me(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        UserResponseDto userResponseDto = new UserResponseDto(customUserDetails.getUsername());
        return ResponseEntity.ok(userResponseDto);
    }
}
