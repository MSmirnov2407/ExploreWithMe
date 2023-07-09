package ru.practicum.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

/**
 * Подборка событий (сущность)
 */
@Entity
@Table(name = "compilations", schema = "public")
@Getter
@Setter
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //id подборки

    @Column(name = "pinned")
    private boolean pinned; //Закреплена ли подборка на главной странице сайта

    @Column(name = "title")
    private String title; //Заголовок подборки

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "event_compilation",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events;
}
