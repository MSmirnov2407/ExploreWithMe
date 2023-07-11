package ru.practicum.controller.publ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDtoWithComments;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@Slf4j
public class EventControllerPublic {
    private final EventService eventService;

    @Autowired
    public EventControllerPublic(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(@RequestParam(name = "text", required = false) String text,
                                         @RequestParam(name = "categories", required = false) List<Integer> categories,
                                         @RequestParam(name = "paid", required = false) Boolean paid,
                                         @RequestParam(name = "rangeStart", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                         @RequestParam(name = "rangeEnd", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                         @RequestParam(name = "onlyAvailable", required = false) Boolean onlyAvailable,
                                         @RequestParam(name = "sort", required = false) String sort,
                                         @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(name = "size", defaultValue = "10") @Positive int size,
                                         HttpServletRequest request) {
        List<EventShortDto> eventDtos = eventService.searchEventsWithStats(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
        log.info("Выполнен поиск событий через публичный API");
        return eventDtos;
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDtoWithComments getEvent(@PathVariable(name = "eventId") int eventId,
                                             HttpServletRequest request) {
        EventFullDtoWithComments eventDto = eventService.getEventByIdWithStats(eventId, request);
        log.info("Получено событие id={} с комментариями к нему, запрос сохранен в сервисе статистики", eventId);
        return eventDto;
    }
}
