package ru.practicum.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EndpointHitMapper {
    private final static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Преобразование EndpointHit в EndpointHitDto
     *
     * @return EndpointHitDto
     */
    public static EndpointHitDto toDto(EndpointHit hit) {
        EndpointHitDto hitDto = new EndpointHitDto();
        /*перекладываем данные в DTO*/
        hitDto.setIp(hit.getIp());
        hitDto.setId(hit.getId());
        hitDto.setApp(hit.getApp());
        hitDto.setUri(hit.getUri());
        /*метку времени превращаем в строку нужного формата*/
        LocalDateTime hitTimestamp = hit.getTimestamp();
        hitDto.setTimestamp(hitTimestamp.format(TIME_FORMAT));
        return hitDto;
    }

    /**
     * Преобразование EndpointHitDto в EndpointHit
     *
     * @return EndpointHit
     */
    public static EndpointHit toHit(EndpointHitDto hitDto) {
        EndpointHit hit = new EndpointHit();
        /*перекладываем данные из DTO в хит*/
        hit.setIp(hitDto.getIp());
        hit.setId(hitDto.getId());
        hit.setApp(hitDto.getApp());
        hit.setUri(hitDto.getUri());

        /*метку времени превращаем из строки в LocalDateTime*/
        String dtoTimestamp = hitDto.getTimestamp();
        hit.setTimestamp(LocalDateTime.parse(dtoTimestamp, TIME_FORMAT));
        return hit;
    }
}
