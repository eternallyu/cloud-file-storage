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

    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters.")
    @NotBlank
    String username;

    @Size(min = 5, max = 20, message = "Password must be between 5 and 20 characters.")
    @NotBlank
    String password;
}
