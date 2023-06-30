package ru.practicum.dto.location;

import ru.practicum.model.Location;

/**
 * Класс, содержащий статические методоы для преобразования объекта Location в его DTO и обратно
 */
public class LocationMapper {

    /**
     * Преобразование Location В LocatinDto
     *
     * @param location - Объект
     * @return - DTO
     */
    public static LocationDto toDto(Location location) {
        LocationDto dto = new LocationDto();

        /*заполнение полей DTO значениями из объекта*/
        dto.setLat(location.getLat());
        dto.setLon(location.getLon());

        return dto;
    }

    /**
     * Преобразование Location В LocatinDto
     *
     * @param locationDto - DTO
     * @return - Объект
     */
    public static Location toLocation(LocationDto locationDto) {
        Location location = new Location();

        /*заполнение полей DTO значениями из объекта*/
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());

        return location;
    }
}
