package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationMapper;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventMapper;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CompilationJpaRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationService {
    private final CompilationJpaRepository compilationJpaRepository;
    private final EventService eventService;
    private final UserService userService;

    /**
     * Получение подборок событий из репозитория
     *
     * @param pinned - параметр для фильтра подборок событий - закреплена ли подборка на гл.странице
     * @param from   - параметр пагинации - с какого элемента выводить
     * @param size   - параметр пагинации - сколько эл-ов выводить
     * @return - список DTO подборок
     */
    public List<CompilationDto> getAllComps(boolean pinned, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending()); //параметризируем переменную для пагинации

        List<Compilation> compilations = compilationJpaRepository.findByPinned(pinned, page); //берем из репозитория нужные подборки
        /*преобразование List<Compilation> в List<CompilationDto>*/
        return compilations.stream()
                .map(CompilationMapper::toDto)
                .collect(Collectors.toList()); //преобразуем в список DTO и возвращаем
    }

    /**
     * Получение подборки событий по id
     *
     * @param compId - id подборки
     * @return - DTO подборки
     */
    public CompilationDto getCompById(int compId) {
        /*проверка параметров запроса*/
        if (compId <= 0) {
            throw new BadParameterException("Id не может быть меньше 1");
        }

        Compilation compilation = compilationJpaRepository.findById(compId)
                .orElseThrow(() -> new ElementNotFoundException("Элемент с id=" + compId + " не найден"));
        return CompilationMapper.toDto(compilation);
    }

    /**
     * Добавление новой подборки
     *
     * @param newCompilationDto - DTO подборки
     * @return - Dto сохраненной подборки
     */
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Set<Integer> newCompilationEventIds = newCompilationDto.getEvents();
        Set<Event> eventSet;
        if (newCompilationEventIds != null && !newCompilationEventIds.isEmpty()) { //сет событий может быть пустым
            Set<EventFullDto> eventDtoSet = eventService.getEventsByIdSet(newCompilationDto.getEvents()); //сет DTO событий
            List<Integer> userIds = eventDtoSet.stream()
                    .map(EventFullDto::getInitiator)
                    .map(UserShortDto::getId)
                    .collect(Collectors.toList());
            List<UserDto> usersDto = userService.getAllUsers(userIds); //получили список UserDto
            List<User> users = usersDto.stream()
                    .map(UserMapper::toUser)
                    .collect(Collectors.toList()); //преобразовали в список User

            Map<Integer, User> eventInitiatorMap = eventDtoSet.stream()
                    .collect(Collectors.toMap(
                            EventFullDto::getId,
                            e -> {
                                return users.stream()
                                        .filter(u -> u.getId() == e.getInitiator().getId())
                                        .findFirst()
                                        .get();
                            }
                    )); //преобразование в мапу id события - User-инициатор

            eventSet = eventDtoSet.stream()
                    .map(e -> EventMapper.toEvent(e, eventInitiatorMap.get(e.getId())))
                    .collect(Collectors.toSet()); //преобразование к сету Event
        } else {
            eventSet = new HashSet<Event>();
        }
        Compilation compilation = compilationJpaRepository.save(CompilationMapper.toComp(newCompilationDto, eventSet)); //сохраняем в ремозитории
        return CompilationMapper.toDto(compilation);
    }

    /**
     * УДаление поборки по id
     *
     * @param compId - id подборки
     */
    public void deleteById(int compId) {
        compilationJpaRepository.findById(compId)
                .orElseThrow(() -> new ElementNotFoundException("Подборка с id= " + compId + " не найден")); //проверка существаования подборки

        compilationJpaRepository.deleteById(compId);
    }

    /**
     * Обновление информации о подборке
     *
     * @param updateRequest - Объект с новой информацией
     * @param compId        - id категории
     * @return - DTO обновленной категории
     */
    @Transactional
    public CompilationDto update(int compId, UpdateCompilationRequest updateRequest) {

        Compilation compilation = compilationJpaRepository.findById(compId)
                .orElseThrow(() -> new ElementNotFoundException("Подборка с id=" + compId + " не найдена")); //Обновляемая подборка

        Set<Integer> eventIds = updateRequest.getEvents();
        Set<Event> eventsSet; //сет событий полученный по сету id
        if (eventIds == null || eventIds.isEmpty()) {
            eventsSet = new HashSet<>();
        } else {
            eventsSet = eventService.getEventsByIds(eventIds); //взяли сет событий по списку id
        }

        /*обновляем значения полей*/
        Boolean pinned = updateRequest.getPinned();
        if (pinned != null) {
            compilation.setPinned(pinned);
        }
        String title = updateRequest.getTitle();
        if (title != null) {
            compilation.setTitle(title);
        }
        compilation.setEvents(eventsSet);
        /*сохраняем в репозитории*/
        compilation = compilationJpaRepository.save(compilation);

        return CompilationMapper.toDto(compilation);
    }
}
