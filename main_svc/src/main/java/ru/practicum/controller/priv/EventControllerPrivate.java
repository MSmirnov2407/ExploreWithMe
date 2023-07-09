package ru.practicum.controller.priv;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.service.EventService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@Slf4j
public class EventControllerPrivate {
    private final EventService eventService;

    @Autowired
    public EventControllerPrivate(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Создание события
     *
     * @param userId      - id пользователя
     * @param newEventDto - DTO события
     * @return - DTO созданного события
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) //201
    public EventFullDto postEvent(@PathVariable(name = "userId") int userId,
                                  @Valid @RequestBody NewEventDto newEventDto) {
        EventFullDto eventDto = eventService.createEvent(newEventDto, userId);
        log.info("Создано новое событие title={}, date={}", eventDto.getTitle(), eventDto.getEventDate());
        return eventDto;
    }

    /**
     * Получение событий, добавленных текущим пользователем
     *
     * @param userId - id пользователя
     * @param from   - параметр пагинации - с какого элемента выводить
     * @param size   - параметр пагинации - сколько эл-ов выводить
     * @return - Список DTO Событий
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK) //200
    public List<EventShortDto> getEventsByUser(@PathVariable(name = "userId") @Positive int userId,
                                               @RequestParam(name = "from", defaultValue = "0") @Positive int from,
                                               @RequestParam(name = "size", defaultValue = "10") @PositiveOrZero int size) {
        List<EventShortDto> eventShortDtos = eventService.getAllByUser(userId, from, size);
        log.info("Получен список событий, добавленных пользователем с id={}", userId);
        return eventShortDtos;
    }

    /**
     * Получение события, добавленного текущим пользователем, по указанному Id
     *
     * @param userId  - id пользователя
     * @param eventId - id события
     * @return - DTO События
     */
    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK) //200
    public EventFullDto getEventByUserAndId(@PathVariable(name = "userId") @Positive int userId,
                                            @PathVariable(name = "eventId") @Positive int eventId) {
        EventFullDto eventFullDto = eventService.getByUserAndId(userId, eventId);
        log.info("Получено событие с Id={} , добавленное пользователем с id={}", eventId, userId);
        return eventFullDto;
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK) //200
    public EventFullDto patchEvent(@PathVariable(name = "userId") @Positive int userId,
                                   @PathVariable(name = "eventId") @Positive int eventId,
                                   @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        EventFullDto eventFullDto = eventService.patchEvent(userId, eventId, updateRequest);
        log.info("Обновлено событие с Id={} , добавленное пользователем с id={}", eventId, userId);
        return eventFullDto;
    }

    /**
     * Получение инфомрации о запросах на участие в событии текущего пользователя
     *
     * @param userId  - id пользователя
     * @param eventId - id события
     * @return - DTO participationRequestDto
     */
    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK) //200
    public List<ParticipationRequestDto> getParticipationInfo(@PathVariable(name = "userId") @Positive int userId,
                                                              @PathVariable(name = "eventId") @Positive int eventId) {
        List<ParticipationRequestDto> partRequestDtoList = eventService.getParticipationInfo(userId, eventId);
        log.info("Получена информация о запросах на учатсие в событии с Id={} , добавленное пользователем с id={}", eventId, userId);
        return partRequestDtoList;
    }

    /**
     * Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
     */
    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK) //200
    public EventRequestStatusUpdateResult patchEventStatus(@PathVariable(name = "userId") @Positive int userId,
                                                           @PathVariable(name = "eventId") @Positive int eventId,
                                                           @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest) {
        EventRequestStatusUpdateResult updateStatusResult = eventService.updateStatus(userId, eventId, statusUpdateRequest);
        log.info("Обновлен статус события с Id={} , добавленное пользователем с id={}. Статус = {}", eventId, userId, statusUpdateRequest.getStatus().toString());
        return updateStatusResult;
    }

}
