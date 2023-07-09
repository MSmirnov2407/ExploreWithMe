package ru.practicum.dto.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;
import ru.practicum.model.Location;

import javax.validation.constraints.Size;

/**
 * Данные для изменения информации о событии. Если поле в запросе не указано (равно null) - значит изменение этих данных не треубется.
 */
@Getter
@Setter
public class UpdateEventAdminRequest {
    @Nullable
    @Size(min = 20, max = 2000)
    private String annotation; //новая аннотация
    private int category; //новая категория
    @Nullable
    @Size(min = 20, max = 7000)
    private String description;   //новое описание
    private String eventDate;//Новые Дата и время на которые намечено событие yyyy-MM-dd HH:mm:ss
    private Location location; //Новое место проведения события
    private Boolean paid; //Новое значение флага о платности мероприятия
    private Integer participantLimit; //новый лимит пользователей
    private Boolean requestModeration; //нужна ли пре-модерация заявок на участие
    private String stateAction; //Новое состояние события
    @Nullable
    @Size(min = 3, max = 120)
    private String title; //Новый Заголовок
}
