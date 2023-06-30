package ru.practicum.dto.user;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Урезанная информация о пользователе
 */
@Getter
@Setter
public class UserShortDto {
    @NotNull
    private int id; //id пользователя
    @NotBlank
    private String name; //имя пользователя
}
