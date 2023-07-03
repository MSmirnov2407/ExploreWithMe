package ru.practicum.controller.priv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.service.EventService;
import ru.practicum.service.ParticipationService;

@RestController
@RequestMapping("/users/{userId}/requests")
@Slf4j
public class ParticipationRequestControllerPrivate {

    private final ParticipationService participationService;
    private final EventService eventService;


    @Autowired
    public ParticipationRequestControllerPrivate(ParticipationService participationService, EventService eventService) {
        this.participationService = participationService;
        this.eventService = eventService;
    }

    /**
     * Создание запроса на участие
     *
     * @param userId  - id пользователя
     * @param eventId - id события
     * @return - DTO запроса
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto postParticipationRequest(@PathVariable(name = "userId") int userId,
                                                            @RequestParam(name = "eventId", required = true) int eventId) {
        EventFullDto eventFullDto = eventService.getEventById(eventId);
        ParticipationRequestDto requestDto = participationService.create(userId, eventFullDto);
        log.info("Создан новый запрос userid={}, eventId={},requestId={}", userId, eventId, requestDto.getId());
        return requestDto;
    }
}
