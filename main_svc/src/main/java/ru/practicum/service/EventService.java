package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.categoty.CategoryMapper;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventMapper;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.CreateConditionException;
import ru.practicum.exception.PaginationParametersException;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.EventJpaRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
public class EventService {
    private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventJpaRepository eventJpaRepository;
    //todo del
//    private final StatsClient statsClient = new StatsClient();

    private final CategoryService categoryService;
    private final UserService userService;

    @Autowired
    public EventService(EventJpaRepository eventJpaRepository, CategoryService categoryService,UserService userService) {
        this.eventJpaRepository = eventJpaRepository;
        this.categoryService = categoryService;
        this.userService = userService;
    }


    /**
     * Создание нового события
     *
     * @param newEventDto - DTO нового события
     * @return - EventFullDto
     */
    public EventFullDto createEvent(NewEventDto newEventDto, int userId) {
        EventFullDto eventFullDto = new EventFullDto();
        /*проверки перед добавлением*/
        LocalDateTime newEventDateTime = LocalDateTime.parse(newEventDto.getEventDate(), TIME_FORMAT); //дата и время из DTO
        if (HOURS.between(LocalDateTime.now(), newEventDateTime) < 2) { //если до события менее 2 часов
            throw new CreateConditionException("Начало события должно быть минимум на два часа позднее текущего момента");
        }
        //todo del
//        if (newUserRequest.getName().isBlank()) {
//            throw new BadParameterException("Поле name не может быть пустым");
//        }
//        String newEmail = newUserRequest.getEmail();
//        if (newEmail.isBlank()) {
//            throw new BadParameterException("Поле email не может быть пустым");
//        }
//        User testUser = userJpaRepository.findByEmail(newEmail);
//        if (testUser != null) {
//            throw new AlreadyExistException("Пользователь с email=" + newEmail + " уже существует");
//        }
//        /*добавление пользователя*/
//        User user = userJpaRepository.save(UserMapper.toUser(newUserRequest));
//        return UserMapper.toDto(user);

//        /*получение категории и пользователя из соответствующих сервисов, чтобы поместить в объект события*/
//        CategoryDto categoryDto = categoryService.getCategoryById(newEventDto.getCategory());
//        Category category = CategoryMapper.toCategory(categoryDto);
//
//        UserDto userDto = userService.getUserById(userId);
//        User user = UserMapper.toUser(userDto);

        /*преобразование newEventDto в FullEventDto*/
        //todo delete
        System.out.println("EventService CreateEvent newEventDto getCategory="+newEventDto.getCategory());

        Category category = CategoryMapper.toCategory(categoryService.getCategoryById(newEventDto.getCategory()));
        User user = UserMapper.toUser(userService.getUserById(userId));

        Event event = EventMapper.toEvent(newEventDto,category, user); //преобразуем в event
//        String[] uris = {"/events/" + event.getId()}; //оформляем uri события в виде списка для дальнейшей передачи в метод statsClient
//        List<EndpointStats> stats = StatsClient.getStats(LocalDateTime.of(1970,01,01,01,01), LocalDateTime.now(), uris, false); //статистика запросов
//        long views = 0;
//        if (!stats.isEmpty()) {
//            views = stats.get(0).getHits(); //берем кол-во просомтров из единственого объекта статистики
//        }

        //todo delete

        System.out.println("EventService CreateEvent event id="+event.getId());
        System.out.println("EventService CreateEvent event Initiatorid="+event.getInitiator().getId());

        Event savedEvent = eventJpaRepository.save(event); //сохранение в репозитории

        //todo delete
        System.out.println("EventService CreateEvent savedEvent id="+savedEvent.getId());
        System.out.println("EventService CreateEvent savedEvent Initiatorid="+savedEvent.getInitiator().getId());

        eventFullDto = EventMapper.toFullDto(savedEvent, 0); //преобразование в EventFullDto
        return eventFullDto;
    }

    /**
     * Получение списка событий, принадлежащих категории по ее id
     *
     * @param catId - id категории
     * @return - список DTO событий
     */
    public List<EventShortDto> getEventsByCategory(int catId) {
        /*проверка параметров запроса*/
        if (catId <= 0) {
            throw new BadParameterException("Id категории должен быть >0");
        }
        List<Event> events = eventJpaRepository.findByCategoryId(catId); //получение из репозитория списка событий заданной категории
        if (events.isEmpty()) {
            return new ArrayList<>(); //если список событий пуск, возвращаем пустой список EventShortDto
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(events.stream().map(Event::getId).collect(Collectors.toList())); // получаем через клиента статистики мапу <id события, кол-во просмотров>

        /*преобразуем список событий в список DTO с указанием кол-ва просмотров*/
        return events.stream()
                .map(e -> EventMapper.toShortDto(e, idViewsMap.get(e.getId())))
                .collect(Collectors.toList());
    }


    /**
     * Получение списка событий, добавленных указанным пользователем
     *
     * @param userId - id пользователя
     * @param from   - параметр пагинации - с какого элемента выводить
     * @param size   - параметр пагинации - сколько эл-ов выводить
     * @return - список DTO
     */
    public List<EventShortDto> getAllByUser(int userId, int from, int size, HttpServletRequest request) {
        /*проверка параметров запроса*/
        if (userId < 0) {
            throw new BadParameterException("Id пользователя должен быть больше 0");
        }
        if (from < 0 || size < 1) { //проверка параметров запроса
            throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
        }
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending()); //параметризируем переменную для пагинации

        /*сохранение данных о запросе в сервисе статистики*/
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("ewm-main-event-service");
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now().format(TIME_FORMAT));
        endpointHitDto.setUri("/events");

        StatsClient.postHit(endpointHitDto); //сохраняем информацию о запросе в сервисе статистики

        /*получаем список событий и кол-во просмотров*/
        List<Event> events = eventJpaRepository.getAllByUser(userId, page); //события, созданные указанным пользователем
        if (events == null || events.isEmpty()) { //если нет событий, возвращаем пустой список
            return new ArrayList<EventShortDto>();
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(events.stream().map(Event::getId).collect(Collectors.toList())); // получаем через клиента статистики мапу <id события, кол-во просмотров>

        return events.stream()
                .map(e -> EventMapper.toShortDto(e, idViewsMap.get(e.getId())))
                .collect(Collectors.toList());
    }
}
