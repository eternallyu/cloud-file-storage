package ru.eternallyu.cloudfilestorage.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {

    @Size(min = 5, max = 20, message = "Имя пользователя и пароль должны содержать от 5 до 20 символов")
    @NotBlank
    String username;

    @Size(min = 5, max = 20, message = "Имя пользователя и пароль должны содержать от 5 до 20 символов")
    @NotBlank
    String password;
}
