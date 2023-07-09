package ru.practicum.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class EndpointHitDto {
    private int id; // id хита
    @NotBlank
    private String app; //Идентификатор сервиса для которого записывается информация
    @NotBlank
    private String uri; //URI для которого был осуществлен запрос
    @NotBlank
    private String ip; //IP-адрес пользователя, осуществившего запрос
    @NotBlank
    private String timestamp; //Дата и время, когда был совершен запрос к эндпоинту yyyy-MM-dd HH:mm:ss

    public EndpointHitDto(String app, String uri, String ip) {
        this.app = app;
        this.uri = uri;
        this.ip = ip;
    }
}
