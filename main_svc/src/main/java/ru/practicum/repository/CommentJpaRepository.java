package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Comment;

import java.util.List;

public interface CommentJpaRepository extends JpaRepository<Comment, Integer> {
    @Query("SELECT c " +
            "FROM Comment as c " +
            "WHERE c.event.id = ?1")
    List<Comment> findAllByEnventId(int eventId);
}
