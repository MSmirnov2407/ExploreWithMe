package ru.practicum.dto.compilation;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.dto.event.EventShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * Подборка событий
 */
@Getter
@Setter
public class CompilationDto {
    @NotNull
    private int id; //id подборки
    @NotNull
    private boolean pinned; //Закреплена ли подборка на главной странице сайта
    @NotBlank
    private String title; //Заголовок подборки
    @NotNull
    private Set<EventShortDto> events = new HashSet<>(); // Список событий входящих в подборку
}
