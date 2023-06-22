package ru.practicum.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hits", schema = "public")
@Getter
@Setter
@ToString
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //id запроса энедопинта
    @Column(name = "app")
    private String app; //Идентификатор сервиса для которого записывается информация
    @Column(name = "uri")
    private String uri; //URI для которого был осуществлен запрос
    @Column(name = "ip")
    private String ip; //IP-адрес пользователя, осуществившего запрос
    @Column(name = "timestamp")
    private LocalDateTime timestamp; //Дата и время, когда был совершен запрос к эндпоинту
}
