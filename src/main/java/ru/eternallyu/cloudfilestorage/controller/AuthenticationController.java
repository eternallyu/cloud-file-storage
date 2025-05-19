package ru.eternallyu.cloudfilestorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.eternallyu.cloudfilestorage.dto.request.UserRequestDto;
import ru.eternallyu.cloudfilestorage.dto.response.UserResponseDto;
import ru.eternallyu.cloudfilestorage.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> signUp(@Valid @RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.saveUser(userRequestDto));
    }

//    @PostMapping("/sign-in")
//    public ResponseEntity<UserResponseDto> signIn(@Valid @RequestBody UserRequestDto userRequestDto) {
//
//    }
}
