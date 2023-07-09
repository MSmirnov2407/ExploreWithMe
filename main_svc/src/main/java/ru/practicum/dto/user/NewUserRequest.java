package ru.practicum.dto.user;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.*;

/**
 * Данные нового пользователя
 */
@Getter
@Setter
public class NewUserRequest {

    @Email
    @Pattern(regexp = ".+@.+\\..+")
    @NotNull
    @Size(min = 6, max = 254)
    private String email; //email
    @NotBlank
    @Size(min = 2, max = 250)
    private String name; //имя пользователя
}
