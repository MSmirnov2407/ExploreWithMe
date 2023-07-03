package ru.practicum.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;

/**
 * Локация - широта и долгота места проведения события (embedded-класс)
 */
@Embeddable
@Getter
@Setter
public class Location {

    private float lat; //lattitude - Широта
    private float lon; //longitude - Долгота
}
