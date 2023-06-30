package ru.practicum.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Заявка на участие в событии (сущность)
 */
@Entity
@Table(name = "participation_requests", schema = "public")
@Getter
@Setter
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //id заявки
    @Column(name = "created")
    private LocalDateTime created; //Дата и время создания заявки
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event")
    private Event event; //Идентификатор события
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester")
    private User requester; //Идентификатор пользователя, отправившего заявку
    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING; //статус заявки
}
