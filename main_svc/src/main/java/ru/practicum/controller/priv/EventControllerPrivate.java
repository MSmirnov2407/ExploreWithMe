package ru.practicum.controller.priv;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
        log.info("Создано новое событие title={}, date={}", newEventDto.getTitle(), newEventDto.getEventDate());

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
    public List<EventShortDto> getEventsByUser(@PathVariable(name = "userId") int userId,
                                               @RequestParam(name = "from", defaultValue = "0") int from,
                                               @RequestParam(name = "size", defaultValue = "10") int size,
                                               HttpServletRequest request) {
        List<EventShortDto> eventShortDtos = eventService.getAllByUser(userId, from, size, request);
        log.info("Получен список событий, добавленных пользователем с id={}", userId);
        return eventShortDtos;
    }
}
