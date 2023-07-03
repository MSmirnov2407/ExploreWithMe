package ru.practicum.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id == user.id && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}
