package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.CreateConditionException;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.model.*;
import ru.practicum.repository.EventJpaRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.HOURS;

@Service
@RequiredArgsConstructor
public class EventService {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventJpaRepository eventJpaRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final ParticipationService participationService;
    private final EntityManager entityManager;

    /**
     * Создание нового события
     *
     * @param newEventDto - DTO нового события
     * @return - EventFullDto
     */
    @Transactional
    public EventFullDto createEvent(NewEventDto newEventDto, int userId) {
        /*проверки перед добавлением*/
        LocalDateTime newEventDateTime = LocalDateTime.parse(newEventDto.getEventDate(), TIME_FORMAT); //дата и время из DTO
        if (HOURS.between(LocalDateTime.now(), newEventDateTime) < 2) { //если до события менее 2 часов
            throw new BadParameterException("Начало события должно быть минимум на два часа позднее текущего момента");
        }
        /*преобразование newEventDto в FullEventDto*/
        Category category = CategoryMapper.toCategory(categoryService.getCategoryById(newEventDto.getCategory()));
        User user = UserMapper.toUser(userService.getUserById(userId));

        Event event = EventMapper.toEvent(newEventDto, category, user); //преобразуем в event
        Event savedEvent = eventJpaRepository.save(event); //сохранение в репозитории

        return EventMapper.toFullDto(savedEvent, 0); //преобразование в EventFullDto
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
                .map(e -> EventMapper.toShortDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
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

        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending()); //параметризируем переменную для пагинации

        /*получаем список событий и кол-во просмотров*/
        List<Event> events = eventJpaRepository.getAllByUser(userId, page); //события, созданные указанным пользователем
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(events.stream()
                .map(Event::getId)
                .collect(Collectors.toList())); // получаем через клиента статистики мапу <id события, кол-во просмотров>

        return events.stream()
                .map(e -> EventMapper.toShortDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
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
        /*получаем событие и кол-во его просмотров*/
        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId); //событие по id и id пользователя
        if (event == null) { //если нет события, кидается исключение
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId())); // получаем через клиента статистики мапу <id события, кол-во просмотров>

        return EventMapper.toFullDto(event, idViewsMap.getOrDefault(event.getId(), 0L));
    }

    /**
     * Получение события по id
     *
     * @param eventId - id события
     * @return - DTO
     */
    public EventFullDto getEventById(int eventId) {
        /*получаем событие и кол-во его просмотров*/
        Event event = eventJpaRepository.findById(eventId)
                .orElseThrow(() -> new ElementNotFoundException("События с id=" + eventId + " не найдено")); //событие по id

        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId())); // получаем через клиента статистики мапу <id события, кол-во просмотров>
        return EventMapper.toFullDto(event, idViewsMap.getOrDefault(event.getId(), 0L));
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
            throw new ElementNotFoundException("Событие с id=" + eventId + " не опубликовано");
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
    @Transactional
    public EventFullDto patchEvent(int userId, int eventId, UpdateEventUserRequest updateRequest) {
        /*получаем событие*/
        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId); //событие по id и id пользователя
        if (event == null) { //если нет события, кидается исключение
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }

        /*проверка допустимого состояния события для изменения*/
        if (event.getState() == EventState.PUBLISHED) {
            throw new DataConflictException("Нельзя обновлять событие в состоянии 'Опубликовано'");
        }

        /*обновление полей события при наличии значений в запросе*/
        String annotation = updateRequest.getAnnotation();
        if (!(annotation == null || annotation.isBlank())) {
            event.setAnnotation(annotation);
        }
        Integer categoryId = updateRequest.getCategory();
        if (categoryId != null && categoryId > 0) { //если catId не ноль, то ищем категорию и присваиваем событию
            CategoryDto categoryDto = categoryService.getCategoryById(categoryId);
            if (categoryDto != null) {
                event.setCategory(CategoryMapper.toCategory(categoryDto));
            }
        }
        /*проверка новой даты события*/
        String newDateString = updateRequest.getEventDate();
        if (!(newDateString == null || newDateString.isBlank())) { //если строка с датой не пустая
            LocalDateTime newDate = LocalDateTime.parse(newDateString, TIME_FORMAT); //преобразуем в дату
            if (HOURS.between(LocalDateTime.now(), newDate) < 2) { //если до события менее 2 часов
                throw new BadParameterException("Начало события должно быть минимум на два часа позднее текущего момента");
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
        if (stateString != null && !stateString.isBlank()) {
            switch (StateActionUser.valueOf(stateString)) {
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
            }
        }
        String title = updateRequest.getTitle();
        if (!(title == null || title.isBlank())) {
            event.setTitle(title);
        }

        eventJpaRepository.save(event); // сохраняем обновленное событие
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId())); // получаем через клиента статистики мапу <id события, кол-во просмотров>

        Event updatedEvent = eventJpaRepository.findById(event.getId())
                .orElseThrow(() -> new ElementNotFoundException("Событие с id=" + event.getId() + " не найден")); //берем из репозитория обновленное событие

        return EventMapper.toFullDto(updatedEvent, idViewsMap.getOrDefault(event.getId(), 0L));
    }

    /**
     * Обновление события администратором
     *
     * @param eventId      - id События
     * @param adminRequest - запрос на изменение от администратора
     * @return - DTO обновленного события
     */
    @Transactional
    public EventFullDto patchAdminEvent(int eventId, UpdateEventAdminRequest adminRequest) {
        /*получаем событие*/
        Event event = eventJpaRepository.findById(eventId)
                .orElseThrow(() -> new ElementNotFoundException("События с id=" + eventId + " не найдено")); //получаем событие по id

        /*обновление полей события при наличии значений в запросе*/
        String annotation = adminRequest.getAnnotation();
        if (!(annotation == null || annotation.isBlank())) {
            event.setAnnotation(annotation);
        }
        int categoryId = adminRequest.getCategory();
        if (categoryId > 0) { //если catId не ноль, то ищем категорию и присваиваем событию
            CategoryDto categoryDto = categoryService.getCategoryById(categoryId);
            if (categoryDto != null) {
                event.setCategory(CategoryMapper.toCategory(categoryDto));
            }
        }
        String description = adminRequest.getDescription();
        if (!(description == null || description.isBlank())) {
            event.setDescription(description);
        }

        /*проверка новой даты события*/
        String newDateString = adminRequest.getEventDate();
        if (!(newDateString == null || newDateString.isBlank())) { //если строка с датой не пустая
            LocalDateTime newDate = LocalDateTime.parse(newDateString, TIME_FORMAT); //преобразуем в дату
            if (HOURS.between(LocalDateTime.now(), newDate) < 2) { //если до события менее 2 часов
                throw new BadParameterException("Начало события должно быть минимум на два часа позднее текущего момента");
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
         * Если в запросе требование на публикацию, проверяем время и публикуем*/
        String stateString = adminRequest.getStateAction();
        if (stateString != null && !stateString.isBlank()) {
            switch (StateActionAdmin.valueOf(stateString)) {
                case PUBLISH_EVENT:
                    if (HOURS.between(LocalDateTime.now(), event.getEventDate()) < 1) { //если до события менее 1 часа
                        throw new CreateConditionException("Начало события должно быть минимум на один час позже момента публикации");
                    }
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new DataConflictException("Попытка опубликовать событие с id=" + event.getId() + ", которое уже опубликовано.");
                    }
                    if (event.getState() == EventState.CANCELED) {
                        throw new DataConflictException("Попытка опубликовать событие с id=" + event.getId() + ", которое уже отменено.");
                    }
                    event.setState(EventState.PUBLISHED);
                    break;
                case REJECT_EVENT:
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new DataConflictException("Попытка отменить событие с id=" + event.getId() + ", которое уже опубликовано.");
                    }
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
        String title = adminRequest.getTitle();
        if (!(title == null || title.isBlank())) {
            event.setTitle(title);
        }

        eventJpaRepository.save(event); // сохраняем обновленное событие
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(List.of(event.getId())); // получаем через клиента статистики мапу <id события, кол-во просмотров>

        Event updatedEvent = eventJpaRepository.findById(event.getId())
                .orElseThrow(() -> new ElementNotFoundException("Событие с id=" + event.getId() + " не найден")); //берем из репозитория обновленное событие

        return EventMapper.toFullDto(updatedEvent, idViewsMap.getOrDefault(event.getId(), 0L));
    }

    /**
     * Получение информации о запросах на участие в мероприятии инициатора
     *
     * @param userId  - id инициатора
     * @param eventId -id события
     * @return - DTO информации о заявках
     */
    @Transactional
    public List<ParticipationRequestDto> getParticipationInfo(int userId, int eventId) {

        /*получаем событие*/
        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId); //событие по id и id инициатора
        if (event == null) { //если нет события, кидается исключение
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }

        return participationService.getAllRequestsEventId(event.getId());
    }

    /**
     * Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя
     *
     * @param userId        - id пользователя
     * @param eventId       - id события
     * @param updateRequest - запрос на обновление
     * @return - результат обновления
     */
    @Transactional
    public EventRequestStatusUpdateResult updateStatus(int userId, int eventId, EventRequestStatusUpdateRequest updateRequest) {
        /*получаем событие*/
        Event event = eventJpaRepository.getByIdAndUserId(eventId, userId); //событие по id и id инициатора
        if (event == null) { //если нет события, кидается исключение
            throw new ElementNotFoundException("События с id=" + eventId + " и initiatorId=" + userId + " не найдено");
        }

        List<ParticipationRequestDto> requests = participationService.getAllRequestsEventId(eventId); //список запросов на участие в событии
        int limit = event.getParticipantLimit();//ограничение участников

        if (updateRequest.getStatus() == UpdateRequestState.REJECTED) { // если обновление подразумевает отклонение заявок
            return rejectRequests(event, requests, updateRequest); //отклоняем заявки и возвращаем результат
        } else { // если обновление подразумевает подтверждение заявок
            if ((limit == 0 || !event.isRequestModeration())) { //если предел участников = 0 или не требуется модерация заявок,
                return confirmAllRequests(event, requests, updateRequest); //подтверждаем все заявки и возвращаем результат
            } else { //требуется учет заявок + обновление подразумевает подтверждение заявок
                return confirmRequests(event, requests, updateRequest); //подтверждаем/отклоняем заявки и возвращаем результат
            }
        }
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
    @Transactional
    public List<EventFullDto> searchEvents(List<Integer> users, List<String> states, List<Integer> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder(); //создаем CriteriaBuilder
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class); //создаем объект CriteriaQuery с помощью CriteriaBuilder
        Root<Event> eventRoot = criteriaQuery.from(Event.class);
        criteriaQuery = criteriaQuery.select(eventRoot);

        /*строим предикаты*/
        List<Event> resultEvents;
        Predicate complexPredicate = null;
        if (rangeStart != null && rangeEnd != null) {
            complexPredicate
                    = criteriaBuilder.between(eventRoot.get("eventDate").as(LocalDateTime.class), rangeStart, rangeEnd);
        }
        if (users != null && !users.isEmpty()) {
            /*строим предикат по инициатору событий*/
            Predicate predicateForUsersId
                    = eventRoot.get("initiator").get("id").in(users);
            if (complexPredicate == null) {
                complexPredicate = predicateForUsersId;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForUsersId); //прикрепили к общему предикату по AND
            }
        }
        if (categories != null && !categories.isEmpty()) {
            /*строим предикат по категории событий*/
            Predicate predicateForCategoryId
                    = eventRoot.get("category").get("id").in(categories);
            if (complexPredicate == null) {
                complexPredicate = predicateForCategoryId;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForCategoryId); //прикрепили к общему предикату по AND
            }
        }
        if (states != null && !states.isEmpty()) {
            Predicate predicateForStates
                    = eventRoot.get("state").as(String.class).in(states);
            if (complexPredicate == null) {
                complexPredicate = predicateForStates;
            } else {
                complexPredicate = criteriaBuilder.and(complexPredicate, predicateForStates); //прикрепили к общему предикату по AND
            }
        }
        if (complexPredicate != null) {
            criteriaQuery.where(complexPredicate); //если были добавлены предикаты, то применяем их к запросу
        }
        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery); //формируем итоговый запрос с построенными по предикатам критериями выборки
        typedQuery.setFirstResult(from); //пагинация
        typedQuery.setMaxResults(size); //пагинация
        resultEvents = typedQuery.getResultList(); //получаем результат запроса

        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(resultEvents.stream().map(Event::getId).collect(Collectors.toList())); // получаем через клиента статистики мапу <id события, кол-во просмотров>

        return resultEvents.stream()
                .map(e -> EventMapper.toFullDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toList());
    }

    /**
     * Возвращает полную информацию о событиях, подходящих под переданные условия.
     * Если не найдено ни одного события, возвращается пустой список.
     * Запрос сохраняется в сервис статистики
     *
     * @param text          -текст для поиска в содержимом аннотации и подробном описании события
     * @param categories    -список id категорий в которых будет вестись поиск
     * @param paid          -поиск только платных/бесплатных событий
     * @param rangeStart    - дата и время не раньше которых должно произойти событие
     * @param rangeEnd      -дата и время не позже которых должно произойти событие
     * @param onlyAvailable - только события у которых не исчерпан лимит запросов на участие
     * @param sort          - Вариант сортировки: по дате события или по количеству просмотров EVENT_DATE, VIEWS
     * @param from          - количество событий, которые нужно пропустить для формирования текущего набора
     * @param size          - количество событий в наборе
     * @return - Список DTO
     */
    @Transactional
    public List<EventShortDto> searchEventsWithStats(String text, List<Integer> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size, HttpServletRequest request) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder(); //создаем CriteriaBuilder
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class); //создаем объект CriteriaQuery с помощью CriteriaBuilder
        Root<Event> eventRoot = criteriaQuery.from(Event.class); //определяем базовую сущность
        criteriaQuery.select(eventRoot); //основываем критерии запросов на выборке данных по базовой сущности

        /*строим предикаты*/
        List<Event> resultEvents;
        Predicate complexPredicate;
        /*предикат дата и время события*/
        if (rangeStart != null && rangeEnd != null) {
            complexPredicate
                    = criteriaBuilder.between(eventRoot.get("eventDate").as(LocalDateTime.class), rangeStart, rangeEnd);
        } else {
            complexPredicate
                    = criteriaBuilder.between(eventRoot.get("eventDate").as(LocalDateTime.class), LocalDateTime.now(), LocalDateTime.of(9999, 1, 1, 1, 1, 1));
        }
        /*предикат по содержанию теста*/
        if (text != null && !text.isBlank()) {
            String decodeText = URLDecoder.decode(text, StandardCharsets.UTF_8); //переводим текст в кодировку UTF_8

            Expression<String> annotationLowerCase = criteriaBuilder.lower(eventRoot.get("annotation"));
            Expression<String> descriptionLowerCase = criteriaBuilder.lower(eventRoot.get("description"));
            Predicate predicateForAnnotation
                    = criteriaBuilder.like(annotationLowerCase, "%" + decodeText.toLowerCase() + "%");
            Predicate predicateForDescription
                    = criteriaBuilder.like(descriptionLowerCase, "%" + decodeText.toLowerCase() + "%");
            Predicate predicateForText = criteriaBuilder.or(predicateForAnnotation, predicateForDescription); //предикаты по ИЛИ
            complexPredicate = criteriaBuilder.and(complexPredicate, predicateForText); //прикрепили к общему предикату по AND
        }
        /*предикат по категории событий*/
        if (categories != null && !categories.isEmpty()) {
            if (categories.stream().anyMatch(c -> c <= 0)) {
                throw new BadParameterException("Id категории должен быть > 0");
            }
            Predicate predicateForCategoryId
                    = eventRoot.get("category").get("id").in(categories);
            complexPredicate = criteriaBuilder.and(complexPredicate, predicateForCategoryId); //прикрепили к общему предикату по AND
        }
        /*предикат по условию необходимости оплаты*/
        if (paid != null) {
            Predicate predicateForPaid
                    = criteriaBuilder.equal(eventRoot.get("paid"), paid);
            complexPredicate = criteriaBuilder.and(complexPredicate, predicateForPaid); //прикрепили к общему предикату по AND

        }
        /*предикат по условию наличия свободных мест*/
        if (onlyAvailable != null) {
            Predicate predicateForOnlyAvailable
                    = criteriaBuilder.lt(eventRoot.get("confirmedRequests"), eventRoot.get("participantLimit"));
            complexPredicate = criteriaBuilder.and(complexPredicate, predicateForOnlyAvailable); //прикрепили к общему предикату по AND
        }
        /*предикат по условию поиска только среди опубликованных событий*/
        Predicate predicateForPublished
                = criteriaBuilder.equal(eventRoot.get("state"), EventState.PUBLISHED);
        complexPredicate = criteriaBuilder.and(complexPredicate, predicateForPublished); //прикрепили к общему предикату по AND

        /*применение всех предикатов к запросу*/
        criteriaQuery.where(complexPredicate);

        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery); //формируем итоговый запрос с построенными по предикатам критериями выборки
        typedQuery.setFirstResult(from); //пагинация
        typedQuery.setMaxResults(size); //пагинация
        resultEvents = typedQuery.getResultList(); //получаем результат запроса

        /*сохранение данных о запросе в сервисе статистики*/
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("ewm-main-event-service");
        endpointHitDto.setIp(request.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now().format(TIME_FORMAT));
        endpointHitDto.setUri(request.getRequestURI());

        StatsClient.postHit(endpointHitDto); //сохраняем информацию о запросе в сервисе статистики

        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(resultEvents.stream().map(Event::getId).collect(Collectors.toList())); // получаем через клиента статистики мапу <id события, кол-во просмотров>

        /*определение режима сортировки*/
        Comparator<EventShortDto> comparator;
        if (sort != null && sort.equals("EVENT_DATE")) {
            comparator = Comparator.comparing(e -> LocalDateTime.parse(e.getEventDate(), TIME_FORMAT));
        } else {
            comparator = Comparator.comparing(EventShortDto::getViews);
        }
        return resultEvents.stream()
                .map(e -> EventMapper.toShortDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public Set<EventFullDto> getEventsByIdSet(Set<Integer> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Event> eventList = eventJpaRepository.findByIdIn(eventIds); //получение списка событий из репозитория

        if (eventList == null || eventList.isEmpty()) { //если нет событий, возвращаем пустой список
            return new HashSet<>();
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(eventList.stream().map(Event::getId).collect(Collectors.toList())); // получаем через клиента статистики мапу <id события, кол-во просмотров>

        return eventList.stream()
                .map(e -> EventMapper.toFullDto(e, idViewsMap.getOrDefault(e.getId(), 0L)))
                .collect(Collectors.toSet());
    }

    /**
     * Получение сета Event-ов по списку id
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

    /**
     * Отклонение заявок на участие в событии
     *
     * @param event         - событие, к которому относятся заявки на участие
     * @param requests      - список DTO заявок на участие
     * @param updateRequest - запрос на изменение статуса заявок на участие
     * @return - результат подтверждения/отклонения заявок на участие в событии
     */
    @Transactional
    private EventRequestStatusUpdateResult rejectRequests(Event event, List<ParticipationRequestDto> requests, EventRequestStatusUpdateRequest updateRequest) {
        EventRequestStatusUpdateResult updateResult = new EventRequestStatusUpdateResult(); //объявление результата метода
        Map<Integer, ParticipationRequestDto> prDtoMap = requests.stream()
                .collect(Collectors.toMap(ParticipationRequestDto::getId, e -> e)); //преобразование списка в мапу <id, Dto>
        for (int id : updateRequest.getRequestIds()) { //для каждого Id из запроса на обновление
            ParticipationRequestDto prDto = prDtoMap.get(id);//берем из списка заявок одну с Id из списка в запросе на обновление
            if (prDto == null) {
                throw new ElementNotFoundException("Запросу на обновление статуса, не найдено событие с id=" + id);
            }
            if (prDto.getStatus().equals(RequestStatus.PENDING.name())) { //если заявка на рассмотрении
                prDto.setStatus(RequestStatus.REJECTED.toString()); // отклоняем
                updateResult.getRejectedRequests().add(prDto); //сложили обработанную заявку в ответ на запрос на обновление
            } else { //иначе исключение
                throw new CreateConditionException("Нельзя отклонить уже обработанную заявку id=" + id);
            }
        }
        participationService.updateAll(updateResult.getRejectedRequests(), event); //сохранили в БД обновленную информацию об отклоненных запросах
        return updateResult;
    }

    /**
     * Подтверждение заявок на участие в событии, БЕЗ учета допустимого количества участников
     *
     * @param event         - событие, к которому относятся заявки на участие
     * @param requests      - список DTO заявок на участие
     * @param updateRequest - запрос на изменение статуса запросов на участие
     * @return - результат подтверждения/отклонения заявок на участие в событии
     */
    @Transactional
    private EventRequestStatusUpdateResult confirmAllRequests(Event event, List<ParticipationRequestDto> requests, EventRequestStatusUpdateRequest updateRequest) {
        int confirmedRequestsAmount = event.getConfirmedRequests(); // текущее кол-во подтвержденных запросов
        EventRequestStatusUpdateResult updateResult = new EventRequestStatusUpdateResult(); //объявление результата метода
        Map<Integer, ParticipationRequestDto> prDtoMap = requests.stream()
                .collect(Collectors.toMap(ParticipationRequestDto::getId, e -> e)); //преобразование списка в мапу <id, Dto>
        for (int id : updateRequest.getRequestIds()) { //для каждого Id из запроса на обновление
            ParticipationRequestDto prDto = prDtoMap.get(id); //берем из списка заявок одну с Id из списка в запросе на обновление
            if (prDto == null) {
                throw new ElementNotFoundException("Запросу на обновление статуса, не найдено событие с id=" + id);
            }
            if (prDto.getStatus().equals(RequestStatus.PENDING.name())) { //если заявка на рассмотрении
                prDto.setStatus(RequestStatus.CONFIRMED.toString()); // подтверждаем
                confirmedRequestsAmount++; //увеличили счетчик подтвержденных заявок
                event.setConfirmedRequests(confirmedRequestsAmount); //сохранили значение в евенте
                eventJpaRepository.save(event); //сохранили в репозитории информацию о событии
            } else { //иначе исключение
                throw new CreateConditionException("Нельзя подтвердить уже обработанную заявку id=" + id);
            }
        }
        participationService.updateAll(updateResult.getConfirmedRequests(), event); //сохранили в БД обновленную информацию о подтвержденных запросах
        return updateResult;
    }

    /**
     * Подтверждение заявок на участие в событии, C учетом допустимого количества участников
     *
     * @param event         - событие, к которому относятся заявки на участие
     * @param requests      - список DTO заявок на участие
     * @param updateRequest - запрос на изменение статуса запросов на участие
     * @return - результат подтверждения/отклонения заявок на участие в событии
     */
    @Transactional
    private EventRequestStatusUpdateResult confirmRequests(Event event, List<ParticipationRequestDto> requests, EventRequestStatusUpdateRequest updateRequest) {
        int confirmedRequestsAmount = event.getConfirmedRequests(); // текущее кол-во подтвержденных запросов
        int limit = event.getParticipantLimit();//ограничение участников
        boolean limitAchieved = false; // флаг достижения лимита по заявкам
        EventRequestStatusUpdateResult updateResult = new EventRequestStatusUpdateResult(); //объявление результата метода
        Map<Integer, ParticipationRequestDto> prDtoMap = requests.stream()
                .collect(Collectors.toMap(ParticipationRequestDto::getId, e -> e)); //преобразование списка в мапу <id, Dto>
        for (int id : updateRequest.getRequestIds()) { //для каждого Id из запроса на обновление
            limitAchieved = confirmedRequestsAmount >= limit; //проверяем флаг достижения ограничения.
            ParticipationRequestDto prDto = prDtoMap.get(id); //берем из списка заявок одну с Id из списка в запросе на обновление
            if (prDto == null) {
                throw new ElementNotFoundException("Запросу на обновление статуса, не найдено событие с id=" + id);
            }
            if (prDto.getStatus().equals(RequestStatus.PENDING.name())) { //если заявка на рассмотрении
                if (limitAchieved) { //если превысили ограничение - все дальнейшие заявки отклоняются
                    prDto.setStatus(RequestStatus.REJECTED.toString()); // отклоняем
                    participationService.update(prDto, event); //сохранили в БД обновленную информацию о запросе
                    updateResult.getRejectedRequests().add(prDto); //сложили обработанную заявку в ответ на запрос на обновление
                } else { //если лимит не превышен - подтверждаем
                    prDto.setStatus(RequestStatus.CONFIRMED.toString()); // подтверждаем
                    confirmedRequestsAmount++; //увеличили счетчик подтвержденных заявок
                    event.setConfirmedRequests(confirmedRequestsAmount); //сохранили значение в евенте
                    eventJpaRepository.save(event); //сохранили в репозитории информацию о событии
                    updateResult.getConfirmedRequests().add(prDto); //сложили обработанную заявку в ответ на запрос на обновление
                }
            } else { //иначе (не PENDING) - исключение
                throw new CreateConditionException("Нельзя подтвердить уже обработанную заявку id=" + id);
            }
        }
        participationService.updateAll(updateResult.getRejectedRequests(), event); //сохранили в БД обновленную информацию об отклоненных запросах
        participationService.updateAll(updateResult.getConfirmedRequests(), event); //сохранили в БД обновленную информацию о подтвержденных запросах
        if (limitAchieved) {
            throw new CreateConditionException("Превышен лимит на кол-во участников. Лимит = " + limit + ", кол-во подтвержденных заявок =" + confirmedRequestsAmount);
        }
        return updateResult;
    }
}
