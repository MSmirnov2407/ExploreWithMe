package ru.practicum.dto.participationRequest;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Изменение статуса запроса на участие в событии текущего пользователя
 */
@Getter
@Setter
public class EventRequestStatusUpdateRequest {
    private List<Integer> requestIds; //Идентификаторы запросов на участие в событии текущего пользователя
    private UpdateRequestState status; //Новый статус запроса на участие в событии текущего пользователя
}
