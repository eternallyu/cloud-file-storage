package ru.eternallyu.cloudfilestorage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
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

    public UserResponseDto findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return userMapper.toUserResponseDto(user);
    }

    public User saveUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Пользователь с таким именем уже существует");
        }
        return userRepository.save(user);
    }
}
