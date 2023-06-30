package ru.practicum.dto.location;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
public class LocationDto {
    @Min(-180)
    @Max(180)
    private float lat; //latitude - Широта
    @Min(-90)
    @Max(90)
    private float lon; //longitude - Долгота
}
