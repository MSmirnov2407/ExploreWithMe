package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO Для передачи параметров запроса в методы сервиса
 */
@Getter
@AllArgsConstructor
public class RequestParamDto {
    private final String start; //время начала выборки статистики
    private final String end; //время конца выборки статистики
    private final String[] uris; //массив URI
    private final boolean unique; //флаг уникальности IP- источников запросов в процессе цчета статистики

//    public RequestParamDto(String start, String end, String[] uris, boolean unique) {
//        this.start = start;
//        this.end = end;
//        this.uris = uris;
//        this.unique = unique;
//    }
}
