package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventMapper;
import ru.practicum.dto.participationRequest.ParticipationMapper;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.CreateConditionException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.model.*;
import ru.practicum.repository.EventJpaRepository;
import ru.practicum.repository.ParticipationJpaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationJpaRepository participationJpaRepository;
    private final EventJpaRepository eventJpaRepository;
    private final UserService userService;

    /**
     * Создание запроса на участие
     *
     * @param userId       - id пользователя
     * @param eventFullDto - DTO события
     * @return - DTO запроса
     */
    public ParticipationRequestDto create(int userId, EventFullDto eventFullDto) {
        ParticipationRequest newPartRequest = new ParticipationRequest();
        if (eventFullDto == null) {
            throw new BadParameterException("Пользователь не найден");
        }
        int eventId = eventFullDto.getId();
        //присваиваем в запрос пользователя, событие и дату создания
        User user = UserMapper.toUser(userService.getUserById(userId));

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
        if (eventFullDto.getParticipantLimit() != 0) { //если ==0, то кол-во участников неограничено
            if (eventFullDto.getConfirmedRequests() >= eventFullDto.getParticipantLimit()) {
                throw new CreateConditionException("У события с id=" + eventId + " достигнут лимит участников " + eventFullDto.getParticipantLimit());
            }
        }

        /*если для события отключена пре-модерация запросов на участие, или нет ограничения,
        то запрос должен автоматически перейти в состояние подтвержденного*/
        if ((eventFullDto.getParticipantLimit() == 0) || (!eventFullDto.isRequestModeration())) {
            newPartRequest.setStatus(RequestStatus.CONFIRMED);
            /*обновляем кол-во подтвержденных заявок у пользователя*/
            int confirmedRequestsAmount = eventFullDto.getConfirmedRequests();
            confirmedRequestsAmount++; //увеличили счетчик подтвержденных заявок
            eventFullDto.setConfirmedRequests(confirmedRequestsAmount); //сохранили значение в евенте

            User eventInitiator = UserMapper.toUser(userService.getUserById(eventFullDto.getInitiator().getId()));
            eventJpaRepository.save(EventMapper.toEvent(eventFullDto, eventInitiator)); //сохранили в ремозитории информацию о событии
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
            throw new BadParameterException("Id собтия должен быть больше 0");
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
     *
     * @param prDto - DTO запроса
     * @param event - событие
     */
    public void update(ParticipationRequestDto prDto, Event event) {
        User user = UserMapper.toUser(userService.getUserById(prDto.getRequester())); //пользователь запрашивающий участие
        participationJpaRepository.save(ParticipationMapper.toPr(prDto, event, user)); //сохраняем обновленную информацию в БД
    }

    /**
     * Получение информации о заявках на уастие текущего пользователя в событиях других пользователей
     *
     * @param userId - id пользователя
     * @return - список заявкок
     */
    public List<ParticipationRequestDto> getRequestsByUser(int userId) {

        UserDto userDto = userService.getUserById(userId); //взяли  пользователя из репозитория по id
        if (userDto == null) {
            throw new ElementNotFoundException("Пользователь с id= " + userId + " не найден");
        }

        List<ParticipationRequest> requestList = participationJpaRepository.findAllByUserId(userId);

        if (requestList == null) { //если заявок нет - возвращаем пустой лист
            return new ArrayList<>();
        }
        return requestList.stream()
                .map(ParticipationMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Отмена своего запроса на участие в событии
     *
     * @param userId    - Id пользвателя
     * @param requestId - id заявки на учстие
     */
    public ParticipationRequestDto patchRequestCancel(int userId, int requestId) {

        UserDto userDto = userService.getUserById(userId); //взяли  пользователя из репозитория по id
        if (userDto == null) {
            throw new ElementNotFoundException("Пользователь с id= " + userId + " не найден");
        }

        ParticipationRequest partRequest = participationJpaRepository.findById(requestId)
                .orElseThrow(() -> new ElementNotFoundException("Заявка на участие с id= " + requestId + " не найден")); //взяли объект
        partRequest.setStatus(RequestStatus.CANCELED); //поставили статус Отменена
        ParticipationRequest partRequestUpdated = participationJpaRepository.save(partRequest); //сохранили в репозитории
        return ParticipationMapper.toDto(partRequestUpdated);
    }
}
