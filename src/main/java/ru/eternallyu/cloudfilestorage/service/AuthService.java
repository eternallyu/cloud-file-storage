package ru.eternallyu.cloudfilestorage.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import ru.eternallyu.cloudfilestorage.dto.request.UserRequestDto;
import ru.eternallyu.cloudfilestorage.dto.response.UserResponseDto;
import ru.eternallyu.cloudfilestorage.entity.User;
import ru.eternallyu.cloudfilestorage.mapper.UserMapper;
import ru.eternallyu.cloudfilestorage.repository.MinioRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy;
    private final AuthenticationManager authenticationManager;

    private final UserService userService;
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;
    private final MinioRepository minioRepository;

    public UserResponseDto signIn(UserRequestDto userRequestDto, HttpServletRequest request, HttpServletResponse response) {

        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userRequestDto.getUsername(), userRequestDto.getPassword()));
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(auth);
        securityContextHolderStrategy.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        return userService.findByUsername(userRequestDto.getUsername());
    }

    public UserResponseDto signUp(UserRequestDto userRequestDto, HttpServletRequest request, HttpServletResponse response) {

        String rawPassword = userRequestDto.getPassword();

        String encode = passwordEncoder.encode(userRequestDto.getPassword());
        userRequestDto = new UserRequestDto(userRequestDto.getUsername(), encode);

        User user = userMapper.toUser(userRequestDto);

        User savedUser = userService.saveUser(user);

        minioRepository.createUserRootFolder(user.getUsername());

        return signIn(new UserRequestDto(savedUser.getUsername(), rawPassword), request, response);
    }
}
