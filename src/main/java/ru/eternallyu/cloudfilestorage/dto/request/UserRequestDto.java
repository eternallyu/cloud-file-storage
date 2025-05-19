package ru.eternallyu.cloudfilestorage.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {

    @Size(min = 3, max = 20, message = "Login must be between 3 and 20 characters")
    private String login;

    @Size(min = 6, max = 20, message = "Password must be between 6 and 20 characters")
    private String password;
}
