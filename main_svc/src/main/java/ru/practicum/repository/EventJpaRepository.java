package ru.practicum.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Event;

import java.util.List;
import java.util.Set;

public interface EventJpaRepository extends JpaRepository<Event, Integer> {

    @Query("SELECT e " +
            "FROM Event AS e " +
            "WHERE e.category.id = ?1")
    List<Event> findByCategoryId(int catId);


    @Query("SELECT e " +
            "FROM Event as e " +
            "WHERE initiator.id = ?1")
    List<Event> getAllByUser(int userId, PageRequest page);

    @Query("SELECT e " +
            "FROM Event as e " +
            "WHERE id = ?1 " +
            "AND initiator.id = ?2")
    Event getByIdAndUserId(int eventId, int userId);

    List<Event> findByIdIn(Set<Integer> eventIds);

    @Query("SELECT e " +
            "FROM Event as e " +
            "JOIN FETCH e.initiator "+
            "WHERE e.id in ?1 ")
    List<Event> findEventsWIthUsersByIdSet(Set<Integer> eventIds);
}
