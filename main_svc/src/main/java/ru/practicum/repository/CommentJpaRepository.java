package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Comment;

import java.util.List;

public interface CommentJpaRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findAllByEventId(int eventId);
}
