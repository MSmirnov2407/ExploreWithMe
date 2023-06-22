package ru.practicum.dto;

import lombok.*;

/**
 * Класс для выгрузки статистики просмотров по конкретному uri
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EndpointStats {
    private String app; //Название сервиса
    private String uri; //URI сервиса
    private long hits; //количество просмотров
}