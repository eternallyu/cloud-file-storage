package ru.eternallyu.cloudfilestorage.http.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.eternallyu.cloudfilestorage.dto.response.UserResponseDto;
import ru.eternallyu.cloudfilestorage.security.CustomUserDetails;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    UserResponseDto getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new UserResponseDto(customUserDetails.getUsername(), customUserDetails.getId());
    }
}
