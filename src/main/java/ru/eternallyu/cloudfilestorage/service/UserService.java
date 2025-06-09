package ru.eternallyu.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.eternallyu.cloudfilestorage.dto.response.UserResponseDto;
import ru.eternallyu.cloudfilestorage.entity.User;
import ru.eternallyu.cloudfilestorage.error.NotFoundException;
import ru.eternallyu.cloudfilestorage.error.UserAlreadyExistsException;
import ru.eternallyu.cloudfilestorage.mapper.UserMapper;
import ru.eternallyu.cloudfilestorage.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponseDto findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return userMapper.toUserResponseDto(user);
    }

    public User saveUser(User user) {
        String username = user.getUsername();
        if (userRepository.findByUsername(username).isPresent()) {
            log.warn("User already exists, username={}", username);
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }
        return userRepository.save(user);
    }
}
