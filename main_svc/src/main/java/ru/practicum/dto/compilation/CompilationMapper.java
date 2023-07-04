package ru.practicum.dto.compilation;

import ru.practicum.client.StatsClient;
import ru.practicum.dto.event.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Класс, содержащий статические методоы для преобразования объекта Compilation в его DTO и обратно
 */
public class CompilationMapper {
    //todo del
//    private final static StatsClient statsClient = new StatsClient();

    /**
     * Преобразование объекта Compilation в CompilationDto
     *
     * @param compilation - исходный объект
     * @return - CompilationDto
     */
    public static CompilationDto toDto(Compilation compilation) {
        CompilationDto compilationDto = new CompilationDto();
        /*заполнение полей DTO значениями из объекта и доп.данных*/
        compilationDto.setId(compilation.getId());
        compilationDto.setPinned(compilation.isPinned());
        compilationDto.setTitle(compilation.getTitle());

        /*составляем список URI событий из подборки*/
        Set<Event> events = compilation.getEvents(); //получили список событий
        if(events == null || events.size() ==0){
            return compilationDto;
        }
        Map<Integer, Long> idViewsMap = StatsClient.getMapIdViews(events.stream().map(Event::getId).collect(Collectors.toList())); // получаем через клиента статистики мапу <id события, кол-во просмотров>
        /*в поле events создаваемого CompilationDto складываем соответствующий set<EventShortDto>*/
        compilationDto.setEvents(compilation.getEvents().stream()
                .map(e -> EventMapper.toShortDto(e, idViewsMap.get(e.getId())))
                .collect(Collectors.toSet()));

        return compilationDto;
    }

    /**
     * Преобразование CompilationDto в Compilation
     *
     * @param CompilationDto - DTO
     * @return - Compilation
     */
    public static Compilation toComp(CompilationDto CompilationDto) {
        Compilation compilation = new Compilation();

        /*заполнение полей объекта значениями из DTO*/
        compilation.setId(CompilationDto.getId());
        compilation.setTitle(CompilationDto.getTitle());
        compilation.setPinned(CompilationDto.isPinned());

        return compilation;
    }


    /**
     * Преобразование NewCompilationDto в Compilation
     *
     * @param newCompilationDto - DTO
     * @return - Compilation
     */
    public static Compilation toComp(NewCompilationDto newCompilationDto, Set<Event> events) {
        Compilation compilation = new Compilation();

        /*заполнение полей Объекта*/
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setEvents(events);
        compilation.setPinned(newCompilationDto.isPinned());

        return compilation;
    }
}


