package ru.practicum.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Пользователь (сущность)
 */
@Entity
@Table(name = "users", schema = "public")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //id пользователя
    @Column(name = "email", unique = true)
    private String email; //email
    @Column(name = "name")
    private String name; //имя пользователя
}
