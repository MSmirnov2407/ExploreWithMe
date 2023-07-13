package ru.practicum.dto.event;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.dto.comment.CommentDto;

import java.util.List;

/**
 * DTO события с полной информацией о нём, Включая комментарии
 */
@Getter
@Setter
public class EventFullDtoWithComments extends EventFullDto {
    private List<CommentDto> comments; //комментарии к событию
}
