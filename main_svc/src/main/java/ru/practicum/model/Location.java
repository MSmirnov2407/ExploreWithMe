package ru.practicum.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Локация - широта и долгота места проведения события (сущность)
 */
@Entity
@Table(name = "location", schema = "public")
@Getter
@Setter
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //id места проведения
    @Column(name = "lat")
    private float lat; //lattitude - Широта
    @Column(name = "lon")
    private float lon; //longitude - Долгота
}
