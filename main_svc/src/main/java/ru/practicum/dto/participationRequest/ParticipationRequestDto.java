package ru.practicum.dto.participationRequest;

import lombok.Getter;
import lombok.Setter;

/**
 * Заявка на участие в событии
 */
@Getter
@Setter
public class ParticipationRequestDto {
    private int id; //id заяки
    private String created; //Дата и время создания заявки в формате 2022-09-06T21:10:05.432
    private int event; //id события
    private int requester; //id пользователя, оставившего заявку
    private String status; //статус заявки
}
