package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.categoty.CategoryDto;
import ru.practicum.dto.categoty.CategoryMapper;
import ru.practicum.dto.event.*;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.dto.participationRequest.UpdateRequestState;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.exception.*;
import ru.practicum.model.*;
import ru.practicum.repository.EventJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
public class EventService {
    private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventJpaRepository eventJpaRepository;


    private final CategoryService categoryService;
    private final UserService userService;
    private final ParticipationService participationService;

    private final EntityManager entityManager;

    @Autowired
    public EventService(EventJpaRepository eventJpaRepository, CategoryService categoryService, UserService userService,
                        ParticipationService participationService, EntityManager entityManager) {
        this.eventJpaRepository = eventJpaRepository;
        this.categoryService = categoryService;
        this.userService = userService;
        this.participationService = participationService;
        this.entityManager = entityManager;
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
        System.out.println("EventService CreateEvent newEventDto getCategory=" + newEventDto.getCategory());

        Category category = CategoryMapper.toCategory(categoryService.getCategoryById(newEventDto.getCategory()));
        User user = UserMapper.toUser(userService.getUserById(userId));

        Event event = EventMapper.toEvent(newEventDto, category, user); //преобразуем в event
//        String[] uris = {"/events/" + event.getId()}; //оформляем uri события в виде списка для дальнейшей передачи в метод statsClient
//        List<EndpointStats> stats = StatsClient.getStats(LocalDateTime.of(1970,01,01,01,01), LocalDateTime.now(), uris, false); //статистика запросов
//        long views = 0;
//        if (!stats.isEmpty()) {
//            views = stats.get(0).getHits(); //берем кол-во просомтров из единственого объекта статистики
//        }

        //todo delete

        System.out.println("EventService CreateEvent event id=" + event.getId());
        System.out.println("EventService CreateEvent event Initiatorid=" + event.getInitiator().getId());

        Event savedEvent = eventJpaRepository.save(event); //сохранение в репозитории

        //todo delete
        System.out.println("EventService CreateEvent savedEvent id=" + savedEvent.getId());
        System.out.println("EventService CreateEvent savedEvent Initiatorid=" + savedEvent.getInitiator().getId());

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
    public List<EventShortDto> getAllByUser(int userId, int from, int size) {
        /*проверка параметров запроса*/
        if (userId < 0) {
            throw new BadParameterException("Id пользователя должен быть больше 0");
        }
        if (from < 0 || size < 1) { //проверка параметров запроса
            throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
        }
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending()); //параметризируем переменную для пагинации

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

    /**
     * Получение события, добавленного указанным пользователем, по соответствующему id
     *
     * @param userId  - id пользователя
     * @param eventId - id События
     * @return - список DTO
     */
    public EventFullDto getByUserAndId(int userId, int eventId) {
        /*проверка параметров запроса*/
        if (userId < 0) {
            throw new BadParameterException("Id пользователя должен быть больше 0");
        }
        if (eventId < 0) {
            throw new BadParameterException("Id события должен быть больше 0");
        }

        /*получаем событие и кол-во его просмотров*/
        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId); //событие по id и id пользователя
        if (event == null) { //если нет события, кидается исключение
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId())); // получаем через клиента статистики мапу <id события, кол-во просмотров>

        return EventMapper.toFullDto(event, idViewsMap.get(event.getId()));
    }


    /**
     * Получение события по id
     *
     * @param eventId - id события
     * @return - DTO
     */
    public EventFullDto getEventById(int eventId) {
        /*проверка параметров запроса*/
        if (eventId < 0) {
            throw new BadParameterException("Id события должен быть больше 0");
        }
        /*получаем событие и кол-во его просмотров*/
        Optional<Event> eventOptional = eventJpaRepository.findById(eventId); //событие по id
        if (eventOptional.isEmpty()) { //если нет события, кидается исключение
            throw new ElementNotFoundException("События с id=" + eventId + " не найдено");
        }
        Event event = eventOptional.get();
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId())); // получаем через клиента статистики мапу <id события, кол-во просмотров>
        return EventMapper.toFullDto(event, idViewsMap.get(event.getId()));
    }

    /**
     * Получение события по id с сохранением факта запроса события в сервисе статистики
     *
     * @param eventId - id события
     * @return - DTO
     */
    public EventFullDto getEventByIdWithStats(int eventId, HttpServletRequest request) {
        EventFullDto eventDto = this.getEventById(eventId); //получили запрашиваемое событие в виде DTO
        if (eventDto.getState() != EventState.PUBLISHED) {
           throw  new ElementNotFoundException("Событие с id="+eventId+" не опубликовано");
        }
        /*сохранение данных о запросе в сервисе статистики*/
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("ewm-main-event-service");
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now().format(TIME_FORMAT));
        endpointHitDto.setUri(request.getRequestURI());

        StatsClient.postHit(endpointHitDto); //сохраняем информацию о запросе в сервисе статистики

        return eventDto;
    }

    /**
     * Обновление события
     *
     * @param userId        - id пользователя
     * @param eventId       - id пользователя
     * @param updateRequest - объект с обновляемыми данными для события
     * @return - список DTO
     */
    public EventFullDto patchEvent(int userId, int eventId, UpdateEventUserRequest updateRequest) {
        /*проверка параметров запроса*/
        if (userId < 0) {
            throw new BadParameterException("Id пользователя должен быть больше 0");
        }
        if (eventId < 0) {
            throw new BadParameterException("Id соытия должен быть больше 0");
        }

        /*получаем событие*/
        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId); //событие по id и id пользователя
        if (event == null) { //если нет события, кидается исключение
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }

        /*провекра допустимого состояния события для изменения*/
        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new DataConflictException("Нельзя обновлять событие в состоянии 'Опубликовано'");
        }

        /*обновление полей события при наличии значений в запросе*/
        String annotation = updateRequest.getAnnotation();
        if (!(annotation == null || annotation.isBlank())) {
            event.setAnnotation(annotation);
        }
        int categoryId = updateRequest.getCategory();
        if (categoryId > 0) { //если catId не ноль, то ищем категорию и прсваиваем событию
            CategoryDto categoryDto = categoryService.getCategoryById(categoryId);
            if (categoryDto != null) {
                event.setCategory(CategoryMapper.toCategory(categoryDto));
            }
        }
        /*провекра новой даты события*/
        String newDateString = updateRequest.getEventDate();
        if (!(newDateString == null || newDateString.isBlank())) { //если строка с датой не пустая
            LocalDateTime newDate = LocalDateTime.parse(newDateString, TIME_FORMAT); //пребразуем в дату
            if (HOURS.between(LocalDateTime.now(), newDate) < 2) { //если до события менее 2 часов
                throw new CreateConditionException("Начало события должно быть минимум на два часа позднее текущего момента");
            }
            event.setEventDate(newDate);
        }
        Location location = updateRequest.getLocation();
        if (location != null) {
            event.setLocation(location);
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        /*если в запросе требование отмены, переводим событие в отмененное состояние
         * Если в запросе требование на ревью, переводим событие в состояние ожидания публикации*/
        String stateString = updateRequest.getStateAction();
        switch (StateActionUser.valueOf(stateString)) {
            case CANCEL_REVIEW:
                event.setState(EventState.CANCELED);
                break;
            case SEND_TO_REVIEW:
                event.setState(EventState.PENDING);
                break;
        }

        String title = updateRequest.getTitle();
        if (!(title == null || title.isBlank())) {
            event.setTitle(title);
        }

        eventJpaRepository.save(event); // сохраняем обновленное событие
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId())); // получаем через клиента статистики мапу <id события, кол-во просмотров>
        Optional<Event> eventOptional = eventJpaRepository.findById(event.getId()); //берем из репозтория обновленное событие

        return EventMapper.toFullDto(eventOptional.get(), idViewsMap.get(event.getId()));
    }

    /**
     * Обновление события администратором
     *
     * @param eventId      - id События
     * @param adminRequest - запрос на изменение от администратора
     * @return - DTO обновленного события
     */
    public EventFullDto patchAdminEvent(int eventId, UpdateEventAdminRequest adminRequest) {
        /*проверка параметров запроса*/
        if (eventId < 0) {
            throw new BadParameterException("Id соытия должен быть больше 0");
        }

        /*получаем событие*/
        Optional<Event> eventOptional = eventJpaRepository.findById(eventId); //полуаем событие по id
        if (eventOptional.isEmpty()) { //если нет события, кидается исключение
            throw new ElementNotFoundException("События с id=" + eventId + " не найдено");
        }
        Event event = eventOptional.get();


        /*обновление полей события при наличии значений в запросе*/
        String annotation = adminRequest.getAnnotation();
        if (!(annotation == null || annotation.isBlank())) {
            event.setAnnotation(annotation);
        }
        int categoryId = adminRequest.getCategory();
        if (categoryId > 0) { //если catId не ноль, то ищем категорию и прсваиваем событию
            CategoryDto categoryDto = categoryService.getCategoryById(categoryId);
            if (categoryDto != null) {
                event.setCategory(CategoryMapper.toCategory(categoryDto));
            }
        }
        String description = adminRequest.getDescription();
        if (!(description == null || description.isBlank())) {
            event.setDescription(description);
        }

        /*провекра новой даты события*/
        String newDateString = adminRequest.getEventDate();
        if (!(newDateString == null || newDateString.isBlank())) { //если строка с датой не пустая
            LocalDateTime newDate = LocalDateTime.parse(newDateString, TIME_FORMAT); //пребразуем в дату
            if (HOURS.between(LocalDateTime.now(), newDate) < 2) { //если до события менее 2 часов
                throw new CreateConditionException("Начало события должно быть минимум на два часа позднее текущего момента");
            }
            event.setEventDate(newDate);
        }
        Location location = adminRequest.getLocation();
        if (location != null) {
            event.setLocation(location);
        }
        if (adminRequest.getPaid() != null) {
            event.setPaid(adminRequest.getPaid());
        }
        if (adminRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(adminRequest.getParticipantLimit());
        }
        if (adminRequest.getRequestModeration() != null) {
            event.setRequestModeration(adminRequest.getRequestModeration());
        }

        /*если в запросе требование отмены, переводим событие в отмененное состояние
         * Если в запросе требование на публикаицю,проверяем время и публикуем*/
        String stateString = adminRequest.getStateAction();
        switch (StateActionAdmin.valueOf(stateString)) {
            case PUBLISH_EVENT:
                if (HOURS.between(LocalDateTime.now(), event.getEventDate()) < 1) { //если до события менее 1 часа
                    throw new CreateConditionException("Начало события должно быть минимум на один час позже момента публикации");
                }
                event.setState(EventState.PUBLISHED);
                break;
            case REJECT_EVENT:
                event.setState(EventState.CANCELED);
                break;
        }

        String title = adminRequest.getTitle();
        if (!(title == null || title.isBlank())) {
            event.setTitle(title);
        }

        eventJpaRepository.save(event); // сохраняем обновленное событие
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId())); // получаем через клиента статистики мапу <id события, кол-во просмотров>
        Optional<Event> updatedEventOptional = eventJpaRepository.findById(event.getId()); //берем из репозтория обновленное событие

        return EventMapper.toFullDto(updatedEventOptional.get(), idViewsMap.get(event.getId()));
    }

    /**
     * Получение информации о запросах на участие в мероприятии инициатора
     *
     * @param userId  - id инициатора
     * @param eventId -id события
     * @return - DTO информации о заявках
     */
    public List<ParticipationRequestDto> getParticipationInfo(int userId, int eventId) {
        /*проверка параметров запроса*/
        if (userId < 0) {
            throw new BadParameterException("Id пользователя должен быть больше 0");
        }
        if (eventId < 0) {
            throw new BadParameterException("Id соытия должен быть больше 0");
        }

        /*получаем событие*/
        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId); //событие по id и id инициатора
        if (event == null) { //если нет события, кидается исключение
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }

        List<ParticipationRequestDto> partDtos = participationService.getALlRequestsEventId(event.getId());
        if (partDtos.isEmpty()) { //если не найдено ни одного запроса, возращается пустой список
            return new ArrayList<>();
        }

        return partDtos;
    }

    /**
     * Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
     *
     * @param userId        - id пользователя
     * @param eventId       - id события
     * @param updateRequest - запрос на обновление
     * @return - результат обновления
     */
    public EventRequestStatusUpdateResult updateStatus(int userId, int eventId, EventRequestStatusUpdateRequest updateRequest) {
        /*проверка параметров запроса*/
        if (userId < 0) {
            throw new BadParameterException("Id пользователя должен быть больше 0");
        }
        if (eventId < 0) {
            throw new BadParameterException("Id соытия должен быть больше 0");
        }

        /*получаем событие*/
        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId); //событие по id и id инициатора
        if (event == null) { //если нет события, кидается исключение
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }

        EventRequestStatusUpdateResult updateResult = new EventRequestStatusUpdateResult(); //объявление результата метода

        List<ParticipationRequestDto> requests = participationService.getALlRequestsEventId(eventId); //список запросов на участие в событии
        int confirmedRequestsAmount = event.getConfirmedRequests(); // текущее кол-во подтвержденных запросов
        int limit = event.getParticipantLimit();//ограничение участников
        boolean limitAchieved = false; // флаг достижения лимита по заявкам

        if (updateRequest.getStatus() == UpdateRequestState.REJECTED) { // если обновление подразумевает отклонение заявок
            for (int id : updateRequest.getRequestIds()) {//для каджого Id из запроса на обновление
                ParticipationRequestDto prDto = requests.stream().filter(pr -> pr.getId() == id).findFirst().orElseThrow(); //берем из списка заявок одну с Id из списка в запросе на обновление
                if (prDto.getStatus().equals(RequestStatus.PENDING.name())) { //если заявка на рассмотрении
                    prDto.setStatus(RequestStatus.REJECTED.toString()); // подтверждаем
                    participationService.update(prDto, event); //сохранили в БД обновленную информациб о запросе
                    updateResult.getRejectedRequests().add(prDto); //сложили обработанную заявку в ответ на запрос на обновление
                } else { //иначе исключение
                    throw new CreateConditionException("Нельзя отклонить уже обработанную заявку id=" + id);
                }
            }
            return updateResult;
        } else { // если обновление подразумевает подтверджение заявок
            if ((limit == 0 || !event.isRequestModeration())) { //если предел участников = 0 или не требуется модерация заявок,
                for (int id : updateRequest.getRequestIds()) {//для каджого Id из запроса на обновление
                    ParticipationRequestDto prDto = requests.stream().filter(pr -> pr.getId() == id).findFirst().orElseThrow(); //берем из списка заявок одну с Id из списка в запросе на обновление
                    if (prDto.getStatus().equals(RequestStatus.PENDING.name())) { //если заявка на рассмотрении
                        prDto.setStatus(RequestStatus.CONFIRMED.toString()); // подтверждаем
                        confirmedRequestsAmount++; //увеличили счетчик подтвержденных заявок
                        event.setConfirmedRequests(confirmedRequestsAmount); //сохранили значение в евенте
                        eventJpaRepository.save(event); //сохранили в ремозитории информацию о событии
                        participationService.update(prDto, event); //сохранили в БД обновленную информациб о запросе
                        updateResult.getConfirmedRequests().add(prDto); //сложили обработанную заявку в ответ на запрос на обновление
                    } else { //иначе исключение
                        throw new CreateConditionException("Нельзя подтвердить уже обработанную заявку id=" + id);
                    }
                }
                return updateResult;
            } else { //требуется учет заявок + обновление подразумевает подтверджение заявок
                for (int id : updateRequest.getRequestIds()) {//для каджого Id из запроса на обновление
                    limitAchieved = confirmedRequestsAmount >= limit; //проверяем флаг достижения ограничения.
                    ParticipationRequestDto prDto = requests.stream().filter(pr -> pr.getId() == id).findFirst().orElseThrow(); //берем из списка заявок одну с Id из списка в запросе на обновление
                    if (prDto.getStatus().equals(RequestStatus.PENDING.name())) { //если заявка на рассмотрении
                        if (limitAchieved) { //если превысили ограничение - все дальнейшие заявки отклоняются
                            prDto.setStatus(RequestStatus.REJECTED.toString()); // отклоняем
                            participationService.update(prDto, event); //сохранили в БД обновленную информациб о запросе
                            updateResult.getRejectedRequests().add(prDto); //сложили обработанную заявку в ответ на запрос на обновление
                        } else { //если лимит не превышен - подтверждаем
                            prDto.setStatus(RequestStatus.CONFIRMED.toString()); // подтверждаем
                            confirmedRequestsAmount++; //увеличили счетчик подтвержденных заявок
                            event.setConfirmedRequests(confirmedRequestsAmount); //сохранили значение в евенте
                            eventJpaRepository.save(event); //сохранили в ремозитории информацию о событии
                            participationService.update(prDto, event); //сохранили в БД обновленную информациб о запросе
                            updateResult.getConfirmedRequests().add(prDto); //сложили обработанную заявку в ответ на запрос на обновление
                        }
                    } else { //иначе (не PENDING) - исключение
                        throw new CreateConditionException("Нельзя подтвердить уже обработанную заявку id=" + id);
                    }
                }
            }
        }

        if (limitAchieved) {
            throw new CreateConditionException("Превышен лимит на кол-во участников. Лимит = " + limit + ", кол-во подтвержденных заявок =" + confirmedRequestsAmount);
        }
        return updateResult; //вернули итоговый результат
    }

    /**
     * Возвращает полную информацию о событиях, подходящих под переданные условия.
     * Если не найдено ни одного события, возвращается пустой список
     *
     * @param users       - список id пользователей, чьи события нужно найти
     * @param states      - список состояний в которых находятся искомые события
     * @param categories- список id категорий в которых будет вестись поиск
     * @param rangeStart  - дата и время не раньше которых должно произойти событие
     * @param rangeEnd    -дата и время не позже которых должно произойти событие
     * @param from        - количество событий, которые нужно пропустить для формирования текущего набора
     * @param size        - количество событий в наборе
     * @return - Список DTO
     */
    public List<EventFullDto> searchEvents(List<Integer> users, List<String> states, List<Integer> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        /*проверка параметров запроса*/
        if (from < 0 || size < 1) { //проверка параметров запроса
            throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
        }
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending()); //параметризируем переменную для пагинации


        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder(); //создаем CriteriaBuilder
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class); //создаем объект CriteriaQuery с помощью CriteriaBuilder
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        criteriaQuery.select(eventRoot);

        //todo del
        System.out.println("EventService перед предикатами!!!!!!!!!!!!!!");


        /*строим предикаты*/
        List<Event> resultEvents = null;
        Predicate complexPredicate = null;
        if (rangeStart != null && rangeEnd != null) {
            Predicate predicateFotDateTime
                    = criteriaBuilder.between(eventRoot.get("eventDate").as(LocalDateTime.class), rangeStart, rangeEnd);
            complexPredicate = predicateFotDateTime;
        }
        if (users != null && !users.isEmpty()) {
            /*берем из репозитория спикок юзеров*/
//            List<User> usersList = userService.getAllUsers(users).stream()
//                    .map(UserMapper::toUser)
//                    .collect(Collectors.toList());
            /*строим предикат по инициатору событий*/
            Predicate predicateFotUsersId
                    = eventRoot.get("initiator").get("id").in(users);
            if (complexPredicate == null) {
                complexPredicate = predicateFotUsersId;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateFotUsersId); //прикрепили к общему предикату по AND
            }
        }
        if (states != null && !states.isEmpty()) {
            Predicate predicateFotStates
                    = eventRoot.get("state").as(String.class).in(states);
            if (complexPredicate == null) {
                complexPredicate = predicateFotStates;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateFotStates); //прикрепили к общему предикату по AND
            }
        }
        if (complexPredicate != null) {
            criteriaQuery.where(complexPredicate); //если были добавлены предикаты, то применяем их к запросу
            resultEvents = entityManager.createQuery(criteriaQuery).getResultList();
        }

        //todo del
        System.out.println("EventService после всех предикатов!!!!!!!!!!!!!!");


        //TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery);
        // List<Event> events = typedQuery.getResultList(); //получили результат сложного запроса
        //todo del
        System.out.println("EventService после получили результат сложного запроса!!!!!!!!!!!!!!");


        if (resultEvents == null || resultEvents.isEmpty()) { //если нет событий, возвращаем пустой список
            return new ArrayList<EventFullDto>();
        }

        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(resultEvents.stream().map(Event::getId).collect(Collectors.toList())); // получаем через клиента статистики мапу <id события, кол-во просмотров>

        return resultEvents.stream()
                .map(e -> EventMapper.toFullDto(e, idViewsMap.get(e.getId())))
                .collect(Collectors.toList());
    }

    /**
     * Возвращает полную информацию о событиях, подходящих под переданные условия.
     * Если не найдено ни одного события, возвращается пустой список.
     * Запрос сохраняется в сервис статистики
     * @param text -текст для поиска в содержимом аннотации и подробном описании события
     * @param categories- список id категорий в которых будет вестись поиск
    * @param paid -поиск только платных/бесплатных событий
     * @param rangeStart  - дата и время не раньше которых должно произойти событие
     * @param rangeEnd    -дата и время не позже которых должно произойти событие
     * @param onlyAvailable - только события у которых не исчерпан лимит запросов на участие
     * @param sort - Вариант сортировки: по дате события или по количеству просмотров EVENT_DATE, VIEWS
     * @param from        - количество событий, которые нужно пропустить для формирования текущего набора
     * @param size        - количество событий в наборе
     * @return - Список DTO
     */
    public List<EventShortDto> searchEventsWithStats(String text, List<Integer> categories, Boolean paid,LocalDateTime rangeStart, LocalDateTime rangeEnd,Boolean onlyAvailable,String sort, int from, int size, HttpServletRequest request){
        /*проверка параметров запроса*/
        if (from < 0 || size < 1) { //проверка параметров запроса
            throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
        }
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending()); //параметризируем переменную для пагинации

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder(); //создаем CriteriaBuilder
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class); //создаем объект CriteriaQuery с помощью CriteriaBuilder
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        criteriaQuery.select(eventRoot);

        /*сохранение данных о запросе в сервисе статистики*/
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("ewm-main-event-service");
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now().format(TIME_FORMAT));
        endpointHitDto.setUri(request.getRequestURI());

        StatsClient.postHit(endpointHitDto); //сохраняем информацию о запросе в сервисе статистики

        return null;
    }

    public Set<EventFullDto> getEventsByIdSet(Set<Integer> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Event> eventList = eventJpaRepository.findByIdIn(eventIds); //получение списка событий из репозитория

        if (eventList == null || eventList.isEmpty()) { //если нет событий, возвращаем пустой список
            return new HashSet<EventFullDto>();
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(eventList.stream().map(Event::getId).collect(Collectors.toList())); // получаем через клиента статистики мапу <id события, кол-во просмотров>

        return eventList.stream()
                .map(e -> EventMapper.toFullDto(e, idViewsMap.get(e.getId())))
                .collect(Collectors.toSet());
    }

    /**
     * Получение сека Event-ов по списку id
     *
     * @param eventIds -сет id событий
     * @return - сет событий
     */
    public Set<Event> getEventsByIds(Set<Integer> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }
        //события выгружаются сразу с инициаторами по EAGER
        List<Event> eventList = eventJpaRepository.findEventsWIthUsersByIdSet(eventIds); //получение списка событий из репозитория
        return new HashSet<>(eventList);
    }


}


//todo перенести в GET /evennts ?

//        /*сохранение данных о запросе в сервисе статистики*/
//        EndpointHitDto endpointHitDto = new EndpointHitDto();
//        endpointHitDto.setApp("ewm-main-event-service");
//        endpointHitDto.setIp(request.getRemoteAddr());
//        endpointHitDto.setTimestamp(LocalDateTime.now().format(TIME_FORMAT));
//        endpointHitDto.setUri("/events");
//
//        StatsClient.postHit(endpointHitDto); //сохраняем информацию о запросе в сервисе статистики
