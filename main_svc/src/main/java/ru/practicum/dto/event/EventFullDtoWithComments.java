package ru.practicum.dto.event;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.dto.categoty.CategoryDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.EventState;
import ru.practicum.model.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO события с полной информацией о нём, Включая комментарии
 */
@Getter
@Setter
public class EventFullDtoWithComments {
    private int id; //id события
    @NotBlank
    private String annotation; //Краткое описание
    @NotNull
    private CategoryDto category; //категория
    private int confirmedRequests; //Количество одобренных заявок на участие в данном событии
    @NotBlank
    private String createdOn; //Дата и время создания события yyyy-MM-dd HH:mm:ss
    @NotBlank
    private String description;   //Полное описание события
    @NotBlank
    private String eventDate;//Дата и время на которые намечено событие yyyy-MM-dd HH:mm:ss
    @NotNull
    private UserShortDto initiator; //пользователь, создавший событие
    @NotNull
    private Location location; //место проведения события
    @NotNull
    private boolean paid; //Нужно ли оплачивать участие
    private int participantLimit = 0; //Ограничение на количество участников. Значение 0 - означает отсутствие ограничения
    @NotBlank
    private String publishedOn; //Дата и время публикации события yyyy-MM-dd HH:mm:ss
    private boolean requestModeration = false; //нужна ли пре-модерация заявок на участие
    private EventState state = EventState.PENDING;//состояние события
    @NotBlank
    private String title; //Заголовок
    private long views; //кол-во просмотров собтытия
    private List<CommentDto> comments; //комментарии к событию
}
