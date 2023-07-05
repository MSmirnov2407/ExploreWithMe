package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.dto.*;
import ru.practicum.repository.StatsJpaRepository;

import javax.persistence.criteria.Expression;
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

        String[] uris = requestParamDto.getUris();

        /**
         * использование specification
         */
//todo удалить лишнее
        for (var e : uris) {
            System.out.println("StatsSERVER - getStats- uri: "+e);
        }

        Specification<EndpointStats> specification = null;
        if (uris != null) {
            for (String word : uris) {
                //todo
                System.out.println("StatsSERVER - getStats- specification: "+word.toLowerCase() + "%");
                Specification<EndpointStats> wordSpecification = (root, query, builder) -> {
                    Expression<String> uriLowerCase = builder.lower(root.get("uri"));
                    return builder.like(uriLowerCase, word.toLowerCase());
                };
                if (specification == null) {
                    specification = wordSpecification;
                } else {
                    specification = specification.or(wordSpecification);
                }
            }
            //todo удалить


            //todo удалить печать
        }
        /*в зависимости от параметров запроса запрашиваем нужные данные*/
        if (requestParamDto.isUnique()) {
            if (uris == null) {
                List<EndpointStats>  result =  statsJpaRepository.getStatsUnique(start, end); //получение статистики уникальные ip БЕЗ фильтра URI
            //todo удалить вывод
                for(var e: result){
                    System.out.println("StatService - getStats - result " +e.getUri());
                }
                return result;
            } else {
                //todo удалить лишнее
                //return statsJpaRepository.getStatsUniqueWithUris(start, end, uris); //получение статистики уникальные ip C фильтром URI
                //todo удалить вывод
                List<EndpointStats>  result = statsJpaRepository.getStatsUniqueWithUris(start, end, specification); //получение статистики уникальные ip C фильтром URI
                for(var e: result){
                    System.out.println("StatService - getStats - result "+e.getUri());
                }
                return result;
            }
        } else { // !unique
            if (uris == null) {
                List<EndpointStats>  result =  statsJpaRepository.getStatsNotUnique(start, end); //получение статистики НЕ уникальные БЕЗ фильтра URI
                //todo удалить вывод
                for(var e: result){
                    System.out.println("StatService - getStats - result "+e.getUri());
                }
                return result;
            } else {
                //todo удалить лишнее

                // return statsJpaRepository.getStatsNotUniqueWithUris(start, end, uris); //получение статистики НЕ уникальные C фильтром URI
                List<EndpointStats>  result = statsJpaRepository.getStatsNotUniqueWithUris(start, end, specification); //получение статистики уникальные ip C фильтром URI
                for(var e: result){
                    System.out.println("StatService - getStats - result "+e.getUri());
                }
                return result;
            }
        }
    }

    //todo del
    /**
     * Внутренний класс для описания спицификации условия для EndPointStats
     */
//    class EndPointStatsSpecification {
//        public Specification<EndpointHit> uriStartsWith(String startWord) {
//            return (root, query, builder) -> {
//                Expression<String> uriLowerCase = builder.lower(root.get("uri"));
//                return builder.like(uriLowerCase, startWord.toLowerCase() + "%");
//            };
//        }
//    }

}
