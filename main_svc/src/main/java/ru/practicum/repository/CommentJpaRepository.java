package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Comment;

public interface CommentJpaRepository extends JpaRepository<Comment, Integer> {

}
