package ru.eternallyu.cloudfilestorage.mapper;

import org.springframework.stereotype.Component;
import ru.eternallyu.cloudfilestorage.dto.request.UserRequestDto;
import ru.eternallyu.cloudfilestorage.dto.response.UserResponseDto;
import ru.eternallyu.cloudfilestorage.entity.User;

@Component
public class UserMapper {

    public User toUser(UserRequestDto userRequestDto) {
        return User.builder()
                .username(userRequestDto.getUsername())
                .password(userRequestDto.getPassword())
                .build();
    }

    public UserResponseDto toUserResponseDto(User user) {
        return UserResponseDto.builder()
                .username(user.getUsername())
                .id(user.getId())
                .build();
    }
}
