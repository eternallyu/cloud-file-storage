package ru.eternallyu.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.eternallyu.cloudfilestorage.dto.request.UserRequestDto;
import ru.eternallyu.cloudfilestorage.dto.response.UserResponseDto;
import ru.eternallyu.cloudfilestorage.entity.User;
import ru.eternallyu.cloudfilestorage.error.UserAlreadyExistsException;
import ru.eternallyu.cloudfilestorage.mapper.UserMapper;
import ru.eternallyu.cloudfilestorage.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public UserResponseDto saveUser(UserRequestDto userRequestDto) {

        String encode = passwordEncoder.encode(userRequestDto.getPassword());
        userRequestDto.setPassword(encode);

        User user = userMapper.toEntity(userRequestDto);

        User savedUser;

        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new UserAlreadyExistsException("User already exists.");
        }

        return userMapper.toDto(savedUser);
    }
}
