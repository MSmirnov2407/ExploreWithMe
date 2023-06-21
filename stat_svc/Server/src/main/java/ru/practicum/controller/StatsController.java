package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.EndpointStats;
import ru.practicum.service.StatsService;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Реализация REST-контроллера сервиса статистики
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    /**
     * Сохранение информации о вызванном эндпоинте
     *
     * @param hitDto - DTO для сохранения
     * @return - текстовый ответ о результате операции
     */
    @PostMapping("/hit")
    public ResponseEntity<String> postHit(@Valid @RequestBody EndpointHitDto hitDto) {
        log.info("Statistic service: сохранен запрос для эндпоинта {}", hitDto.getUri()); //логируем
        statsService.saveHit(hitDto); //вызываем метод сервиса, для сохранения инф.об эндпоинте
        return new ResponseEntity<>("Информация сохранена", HttpStatus.CREATED); //возвращаем текстовый ответ
    }

    /**
     * Получение статистики по запрошенным URI за заданный период.
     *
     * @param start  - начало периода
     * @param end    - окончание периода
     * @param uris   - список URI, для которых требуется статтистика (необязательный параметр)
     * @param unique - флаг учета только запроса с уникальных IP-адресов (по умолчанию = false)
     * @return список объектов статистики ViewStats по каждому URI
     * @throws UnsupportedEncodingException - в случае неправильно кодировка параметров start, end
     */
    @GetMapping("/stats")
    public List<EndpointStats> getStats(@RequestParam(name = "start") String start,
                                        @RequestParam(name = "end") String end,
                                        @RequestParam(name = "uris", required = false) String[] uris,
                                        @RequestParam(name = "unique", defaultValue = "false") boolean unique) throws UnsupportedEncodingException {
        log.info("Statistic service: запрошена статистика для эндпоинтов {}", uris); //логируем
        return statsService.getStats(start, end, uris, unique); //получаем статистику от сервиса
    }


    /**
     * Запрос всей имеющейся статистики (unique = false)
     *
     * @return - статистика по всем URI
     */
    @GetMapping("/hits")
    public List<EndpointHitDto> getAllHits() {
        log.info("Statistic service: запрошена вся статистика"); //логируем
        return statsService.getAllHits();
    }
}
