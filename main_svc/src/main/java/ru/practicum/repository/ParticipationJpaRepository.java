package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

@Repository
public interface ParticipationJpaRepository extends JpaRepository<ParticipationRequest, Integer> {

    @Query("SELECT pr " +
            "FROM ParticipationRequest as pr " +
            "WHERE pr.requester.id = ?1 " +
            "AND pr.event.id = ?2")
    ParticipationRequest getByUserIdAndEventId(int userId, int eventId);

    @Query("SELECT pr " +
            "FROM ParticipationRequest as pr " +
            "WHERE pr.event.id = ?1")
    List<ParticipationRequest> findAllByEventId(int eventId);

    @Query("SELECT pr " +
            "FROM ParticipationRequest as pr " +
            "WHERE pr.requester.id = ?1")
    List<ParticipationRequest> findAllByUserId(int userId);
}


