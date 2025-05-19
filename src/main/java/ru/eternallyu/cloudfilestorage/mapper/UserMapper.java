package ru.eternallyu.cloudfilestorage.mapper;

import org.mapstruct.Mapper;
import ru.eternallyu.cloudfilestorage.dto.request.UserRequestDto;
import ru.eternallyu.cloudfilestorage.dto.response.UserResponseDto;
import ru.eternallyu.cloudfilestorage.entity.User;

@Mapper
public interface UserMapper {
    User toEntity(UserRequestDto userRequestDto);

    UserResponseDto toDto(User savedUser);
}
