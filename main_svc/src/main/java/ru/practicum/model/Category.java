package ru.practicum.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Категория (сущность)
 */
@Entity
@Table(name = "categories", schema = "public")
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; // id категории

    @Column(name = "name", unique = true)
    private String name; //название категории


}
