package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointHitMapper;
import ru.practicum.dto.EndpointStats;
import ru.practicum.dto.RequestParamDto;
import ru.practicum.repository.StatsJpaRepository;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatsService {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatsJpaRepository statsJpaRepository; //репозиторий для хранения обращений к эндпоинтам


    @Autowired
    public StatsService(StatsJpaRepository statsJpaRepository) {
        this.statsJpaRepository = statsJpaRepository;
    }

    /**
     * Сохранения информации о вызываемом эндпоинте
     *
     * @param hitDto - DTO эндпоинта
     */
    public void saveHit(EndpointHitDto hitDto) {
        statsJpaRepository.save(EndpointHitMapper.toHit(hitDto)); //преобразуем DTO в сущность и сохраняем в базу
    }

    /**
     * Выгрузка всех запросов из БД
     *
     * @return список EndpointHitDto
     */
    public List<EndpointHitDto> getAllHits() {
        return statsJpaRepository.findAll().stream()
                .map(EndpointHitMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение статистики о запрашиваемых эндпоинтах
     *
     * @param requestParamDto - DTO для передачи параметров запроса в методы сервисов
     * @return - список объектов ViewStats с информацией о статистике запросов
     */
    public List<EndpointStats> getStats(RequestParamDto requestParamDto) {
        /*преобразование зашифрованных строк в стандартную кодировку*/
        String startDecoded = URLDecoder.decode(requestParamDto.getStart(), StandardCharsets.UTF_8);
        String endDecoded = URLDecoder.decode(requestParamDto.getEnd(), StandardCharsets.UTF_8);

        /*преобразование полученных строк в LocalDateTime*/
        LocalDateTime start = LocalDateTime.parse(startDecoded, TIME_FORMAT);
        LocalDateTime end = LocalDateTime.parse(endDecoded, TIME_FORMAT);

        //проверка параметров
        if (start.isAfter(end)) {
            throw new RuntimeException("время начала не может быть поздне, чем  время конца выборки");
        }

        String[] uris = requestParamDto.getUris();

        /*в зависимости от параметров запроса запрашиваем нужные данные*/
        if (requestParamDto.isUnique()) {
            if (uris == null) {
                return statsJpaRepository.getStatsUnique(start, end); //получение статистики уникальные ip БЕЗ фильтра URI
            } else {
                return statsJpaRepository.getStatsUniqueWithUris(start, end, uris); //получение статистики уникальные ip C фильтром URI
            }
        } else { // !unique
            if (uris == null) {
                return statsJpaRepository.getStatsNotUnique(start, end); //получение статистики НЕ уникальные БЕЗ фильтра URI
            } else {
                return statsJpaRepository.getStatsNotUniqueWithUris(start, end, uris); //получение статистики уникальные ip C фильтром URI
            }
        }
    }
}
