package ru.practicum.dto.event;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.dto.categoty.CategoryDto;
import ru.practicum.dto.user.UserShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO события с краткой информацией о нём
 */
@Getter
@Setter
public class EventShortDto {
    private int id; //id события
    @NotBlank
    private String annotation; //Краткое описание
    @NotNull
    private CategoryDto category; //категория
    private int confirmedRequests; //Количество одобренных заявок на участие в данном событии
    @NotBlank
    private String eventDate;//Дата и время на которые намечено событие yyyy-MM-dd HH:mm:ss
    @NotNull
    private UserShortDto initiator; //пользователь, создавший событие
    @NotNull
    private boolean paid; //Нужно ли оплачивать участие
    @NotBlank
    private String title; //Заголовок
    private long views; //кол-во просмотров собтытия
}
