package ru.practicum.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class EndpointHitDto {
    @NotNull
    private int id; // id хита
    @NotBlank
    private String app; //Идентификатор сервиса для которого записывается информация
    @NotBlank
    private String uri; //URI для которого был осуществлен запрос
    @NotBlank
    private String ip; //IP-адрес пользователя, осуществившего запрос
    @NotBlank
    private String timestamp; //Дата и время, когда был совершен запрос к эндпоинту yyyy-MM-dd HH:mm:ss
}
