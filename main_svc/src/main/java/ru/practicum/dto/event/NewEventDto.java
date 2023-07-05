package ru.practicum.dto.event;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.model.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO новое событие
 */
@Getter
@Setter
public class NewEventDto {
    @NotNull
    @Size(min = 20, max = 2000)
    private String annotation; //Краткое описание
    @NotNull
    private int category; //категория
    @NotNull
    @Size(min = 20, max = 7000)
    private String description;   //Полное описание события
    @NotBlank
    private String eventDate;//Дата и время на которые намечено событие yyyy-MM-dd HH:mm:ss
    @NotNull
    private Location location; //место проведения события
    @NotNull
    private boolean paid = false; //Нужно ли оплачивать участие
    private int participantLimit = 0; //Ограничение на количество участников. Значение 0 - означает отсутствие ограничения
    private boolean requestModeration = true; //нужна ли пре-модерация заявок на участие
    @Size(min = 3, max = 120)
    private String title; //Заголовок
}
