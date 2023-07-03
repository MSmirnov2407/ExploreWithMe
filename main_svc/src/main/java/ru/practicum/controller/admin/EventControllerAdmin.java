package ru.practicum.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.service.EventService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@Slf4j
public class EventControllerAdmin {

    private final EventService eventService;

    @Autowired
    public EventControllerAdmin(EventService eventService) {
        this.eventService = eventService;
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto patchEvent(@PathVariable(name = "eventId") int eventId,
                                   @Valid @RequestBody UpdateEventAdminRequest adminRequest) {
        EventFullDto eventFullDto = eventService.patchAdminEvent(eventId, adminRequest);
        log.info("Админ обновил событие с Id={}", eventId);
        return eventFullDto;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> searchEvents (@RequestParam(name = "users", required = false) List<Integer> users,
                                            @RequestParam(name = "states", required = false) List<String> states,
                                            @RequestParam(name = "categories", required = false) List<Integer> categories,
                                            @RequestParam(name = "rangeStart", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                            @RequestParam(name = "rangeEnd", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                            @RequestParam(name = "from", required = false, defaultValue = "0") int from,
                                            @RequestParam(name = "size", required = false,defaultValue = "10") int size){
        List<EventFullDto> events = eventService.searchEvents(users,states,categories,rangeStart,rangeEnd,from,size);
        log.info("Выполнен поиск событий");
        return events;
    }

}
