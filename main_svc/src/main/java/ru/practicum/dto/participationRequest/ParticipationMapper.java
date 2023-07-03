package ru.practicum.dto.participationRequest;

import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.RequestStatus;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParticipationMapper {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Преобразование объекта ParticipationRequest в ParticipationRequestDto
     *
     * @param partRequest - объект
     * @return - DTO
     */
    public static ParticipationRequestDto toDto(ParticipationRequest partRequest) {
        ParticipationRequestDto dto = new ParticipationRequestDto();

        /*заполнение полей DTO Значениями из объекта*/
        dto.setId(partRequest.getId());
        dto.setCreated(partRequest.getCreated().format(TIME_FORMAT));
        dto.setEvent(partRequest.getEvent().getId());
        dto.setRequester(partRequest.getRequester().getId());
        dto.setStatus(partRequest.getStatus().toString());

        return dto;
    }

    /**
     * Преобразование DTO ParticipationRequestDto в ParticipationRequest
     *
     * @param dto - DTO Запроса на участие
     * @param event - событие
     * @param user - пользователь на участие
     * @return - запрос на участие
     */
    public static ParticipationRequest toPr(ParticipationRequestDto dto, Event event, User user) {
        ParticipationRequest pr = new ParticipationRequest();

        /*заполнение полей DTO Значениями из объекта*/
        pr.setId(dto.getId());
        pr.setCreated(LocalDateTime.parse(dto.getCreated(),TIME_FORMAT));
        pr.setEvent(event);
        pr.setRequester(user);
        pr.setStatus(RequestStatus.valueOf(dto.getStatus()));

        return pr;
    }
}
