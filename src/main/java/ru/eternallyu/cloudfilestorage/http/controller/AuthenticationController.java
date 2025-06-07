package ru.eternallyu.cloudfilestorage.http.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.eternallyu.cloudfilestorage.dto.request.UserRequestDto;
import ru.eternallyu.cloudfilestorage.dto.response.UserResponseDto;
import ru.eternallyu.cloudfilestorage.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    UserResponseDto signUp(@Valid @RequestBody UserRequestDto userRequestDto) {
        return authService.signUp(userRequestDto);
    }

    @PostMapping("/sign-in")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    UserResponseDto signIn(@Valid @RequestBody UserRequestDto userRequestDto,
                           HttpServletRequest request,
                           HttpServletResponse response) {
        return authService.signIn(userRequestDto, request, response);
    }
}
