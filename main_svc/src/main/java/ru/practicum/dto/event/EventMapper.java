package ru.practicum.dto.event;

import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.dto.categoty.CategoryDto;
import ru.practicum.dto.categoty.CategoryMapper;
import ru.practicum.dto.location.LocationMapper;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.service.CategoryService;
import ru.practicum.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

/**
 * Класс, содержащий статические методоы для преобразования объекта Event в его DTO и обратно
 */
public class EventMapper {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private static CategoryService categoryService;
    @Autowired
    private static UserService userService;

    //todo удалить или поправить
//
//    public static Event toEvent(EventShortDto shortDto) {
//        Event event = new Event();
///*заполняем поля объекта значениями из DTO*/
//        event.setId(shortDto.getId());
//        event.setEventDate(LocalDateTime.parse(shortDto.getEventDate(), TIME_FORMAT));
//        event.setAnnotation(shortDto.getAnnotation());
//        event.setCompilations(shortDto.get);
//        return event;
//    }

    public static Event toEvent(NewEventDto newEventDto, int userId) {
        Event event = new Event();

        /*заполняем поля объекта значениями из DTO*/
        event.setAnnotation(newEventDto.getAnnotation());
        event.setEventDate(LocalDateTime.now());
        event.setLocation(LocationMapper.toLocation(newEventDto.getLocation()));
        event.setPaid(newEventDto.isPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.isRequestModeration());
        event.setTitle(newEventDto.getTitle());
        event.setCompilations(new HashSet<Compilation>());

        //todo del
        System.out.println("EventMapper перед категориям");

        CategoryDto categoryDto = categoryService.getCategoryById(newEventDto.getCategory());
        //todo del
        System.out.println("EventMapper после categoryService, перед CategoryMapper");
        Category category = CategoryMapper.toCategory(categoryDto);

        //todo del
        System.out.println("EventMapper после категорий, перед юзерами");

        UserDto userDto = userService.getUserById(userId);
        User user = UserMapper.toUser(userDto);

        //todo del
        System.out.println("EventMapper после юзеров");

        event.setCategory(category);
        event.setInitiator(user);

        return event;
    }

    /**
     * Преобразование объекта Event в EventShortDto
     *
     * @param event - Объект события
     * @return - DTO события
     */
    public static EventShortDto toShortDto(Event event, long views) {
        EventShortDto shortDto = new EventShortDto();

        /*заполняем поля DTO значениями из объекта*/
        shortDto.setId(event.getId());
        shortDto.setAnnotation(event.getAnnotation());
        shortDto.setCategory(CategoryMapper.toDto(event.getCategory()));
        shortDto.setConfirmedRequests(event.getConfirmedRequests());
        shortDto.setEventDate(event.getEventDate().format(TIME_FORMAT));
        shortDto.setInitiator(UserMapper.toShortDto(event.getInitiator()));
        shortDto.setPaid(event.isPaid());
        shortDto.setTitle(event.getTitle());
        shortDto.setViews(views);

        return shortDto;
    }

    /**
     * Преобразование объекта Event в EventFullDto
     *
     * @param event - Объект события
     * @return - DTO события
     */
    public static EventFullDto toFullDto(Event event, long views) {
        EventFullDto eventFullDto = new EventFullDto();

        /*заполняем поля DTO значениями из объекта*/
        eventFullDto.setId(event.getId());
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCategory(CategoryMapper.toDto(event.getCategory()));
        eventFullDto.setConfirmedRequests(event.getConfirmedRequests());
        eventFullDto.setCreatedOn(event.getCreatedOn().format(TIME_FORMAT));
        eventFullDto.setInitiator(UserMapper.toShortDto(event.getInitiator()));
        eventFullDto.setLocation(LocationMapper.toDto(event.getLocation()));
        eventFullDto.setPaid((event.isPaid()));
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setPublishedOn(event.getPublishedOn().format(TIME_FORMAT));
        eventFullDto.setRequestModeration(event.isRequestModeration());
        eventFullDto.setState(event.getState());
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setViews(views);

        return eventFullDto;
    }


}
