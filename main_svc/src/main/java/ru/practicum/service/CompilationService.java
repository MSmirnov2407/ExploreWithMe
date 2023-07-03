package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationMapper;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.exception.PaginationParametersException;
import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompilationService {
    private final CompilationJpaRepository compilationJpaRepository;
    //todo del
    //private final StatsClient statsClient = new StatsClient(); //клинет для обращения к сервису статистики

    @Autowired
    public CompilationService(CompilationJpaRepository compilationJpaRepository,
                              EventService eventService) {
        this.compilationJpaRepository = compilationJpaRepository;

    }

    /**
     * Получение подборок событий из репозитория
     *
     * @param pinned - параметр для фильтра подборок событий - закреплена ли подборка на гл.странице
     * @param from   - параметр пагинации - с какого элемента выводить
     * @param size   - параметр пагинации - сколько эл-ов выводить
     * @return - список DTO подборок
     */
    public List<CompilationDto> getAllComps(boolean pinned, int from, int size) {
        if (from < 0 || size < 1) { //проверка параметров запроса
            throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
        }
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
        /*запрос подборки из репозитория*/
        Optional<Compilation> compilationOptional = compilationJpaRepository.findById(compId);
        /*проверка наличия искомого элемента*/
        if (compilationOptional.isEmpty()) {
            throw new ElementNotFoundException("Элемент с id=" + compId + " не найден");
        }
        return CompilationMapper.toDto(compilationOptional.get());
    }
}
