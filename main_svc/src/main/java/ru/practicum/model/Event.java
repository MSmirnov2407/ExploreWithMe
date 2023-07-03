package ru.practicum.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Событие (сущность)
 */
@Entity
@Table(name = "events", schema = "public")
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //id события

    @Column(name = "annotation")
    private String annotation; //Краткое описание

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category")
    private Category category; //категория

    @Column(name = "confirmed_requests")
    private int confirmedRequests; //Количество одобренных заявок на участие в данном событии

    @Column(name = "created_on")
    private LocalDateTime createdOn; //Дата и время создания события
    @Column(name = "description")
    private String description;   //Полное описание события

    @Column(name = "event_date")
    private LocalDateTime eventDate;//Дата и время на которые намечено событие

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator")
    private User initiator; //пользователь, создавший событие

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lat", column = @Column(name = "location_lat")),
            @AttributeOverride(name = "lon", column = @Column(name = "location_lon"))})
    private Location location; //место проведения события

    @Column(name = "paid")
    private boolean paid; //Нужно ли оплачивать участие

    @Column(name = "participant_limit")
    private int participantLimit; //Ограничение на количество участников. Значение 0 - означает отсутствие ограничения

    @Column(name = "published_on")
    private LocalDateTime publishedOn; //Дата и время публикации события

    @Column(name = "request_moderation")
    private boolean requestModeration; //нужна ли пре-модерация заявок на участие
    @Enumerated(EnumType.STRING)
    private EventState state = EventState.PENDING;//состояние события

    @Column(name = "title")
    private String title; //Заголовок

    @ManyToMany(mappedBy = "events")
    //связанное поле в сущности Compilations. Там же описана и связка через пром.таблицу
    private Set<Compilation> compilations;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return id == event.id && initiator.equals(event.initiator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, initiator);
    }
}
