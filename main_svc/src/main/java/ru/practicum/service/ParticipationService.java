package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventMapper;
import ru.practicum.dto.participationRequest.ParticipationMapper;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.CreateConditionException;
import ru.practicum.model.*;
import ru.practicum.repository.ParticipationJpaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParticipationService {

    private final ParticipationJpaRepository participationJpaRepository;
    //    private final EventService eventService;
    private final UserService userService;


    @Autowired
    public ParticipationService(ParticipationJpaRepository participationJpaRepository,
                                UserService userService, CategoryService categoryService) {
        this.participationJpaRepository = participationJpaRepository;
//        this.eventService = eventService;
        this.userService = userService;
    }

    /**
     * Создание запроса на участие
     *
     * @param userId       - id пользователя
     * @param eventFullDto - DTO события
     * @return - DTO запроса
     */
    public ParticipationRequestDto create(int userId, EventFullDto eventFullDto) {
        ParticipationRequest newPartRequest = new ParticipationRequest();
        int eventId = eventFullDto.getId();
        //присваиваем в запрос пользователя, событие и дату создания
        User user = UserMapper.toUser(userService.getUserById(userId));
//        EventFullDto eventFullDto = eventService.getEventById(eventId);

        newPartRequest.setRequester(user);
        newPartRequest.setEvent(EventMapper.toEvent(eventFullDto, user));
        newPartRequest.setCreated(LocalDateTime.now());

        /*проверка параметров запроса*/
        /*нельзя добавлять запрос повторно*/
        ParticipationRequest duplicatedRequest = participationJpaRepository.getByUserIdAndEventId(userId, eventId);
        if (duplicatedRequest != null) {
            throw new CreateConditionException("Запрос от пользователя id=" + userId + " на событие c id=" + eventId + " уже существует");
        }
        /*инициатор события не может добавить запрос на участие в своём событии */
        if (eventFullDto.getInitiator().getId() == userId) { //если событие существует и создатель совпадает по id с пользователем
            throw new CreateConditionException("Пользователь не может создавать запрос на участие в своем событии");
        }
        /*нельзя участвовать в неопубликованном событии*/
        if (eventFullDto.getState() != EventState.PUBLISHED) {
            throw new CreateConditionException("Событие с id=" + eventId + " не опубликовано");
        }
        /*нельзя участвовать при превышении лимита заявок*/
        if (eventFullDto.getConfirmedRequests() >= eventFullDto.getParticipantLimit()) {
            throw new CreateConditionException("У события с id=" + eventId + " достигнут лимит участников " + eventFullDto.getParticipantLimit());
        }

        /*если для события отключена пре-модерация запросов на участие,
        то запрос должен автоматически перейти в состояние подтвержденного*/
        if (!eventFullDto.isRequestModeration()) {
            newPartRequest.setStatus(RequestStatus.CONFIRMED);
        }

        ParticipationRequest partRequest = participationJpaRepository.save(newPartRequest);
        return ParticipationMapper.toDto(partRequest);
    }

    /**
     * получение всех заявок на участик по Id события
     *
     * @param eventId - id События
     * @return - список DTO заявок на участие
     */
    public List<ParticipationRequestDto> getALlRequestsEventId(int eventId) {
        /*проверка параметров запроса*/
        if (eventId < 0) {
            throw new BadParameterException("Id соытия должен быть больше 0");
        }

        List<ParticipationRequest> partRequests = participationJpaRepository.findAllByEventId(eventId); //запрашиваем все запросы на событие
        if (partRequests == null || partRequests.isEmpty()) { //если запросов нет, возвращаем пустой список
            return new ArrayList<>();
        }
        /*преобразуем в DTO и возвращаем*/
        return partRequests.stream()
                .map(ParticipationMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Обновление информации о запросе в репозитории
     * @param prDto - DTO запроса
     * @param event - событие
     */
    public void update(ParticipationRequestDto prDto, Event event) {
        User user = UserMapper.toUser(userService.getUserById(prDto.getRequester())); //пользователь запрашивающий участие
        participationJpaRepository.save(ParticipationMapper.toPr(prDto, event, user )); //сохраняем обновленную информацию в БД
    }
}