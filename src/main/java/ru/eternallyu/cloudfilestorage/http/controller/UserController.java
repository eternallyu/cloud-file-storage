package ru.eternallyu.cloudfilestorage.http.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.eternallyu.cloudfilestorage.dto.response.UserResponseDto;
import ru.eternallyu.cloudfilestorage.security.CustomUserDetails;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    UserResponseDto getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return new UserResponseDto(customUserDetails.getUsername(), customUserDetails.getId());
    }
}
