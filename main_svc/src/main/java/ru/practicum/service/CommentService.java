package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentMapper;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CommentJpaRepository;
import ru.practicum.repository.EventJpaRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentJpaRepository commentJpaRepository;
    private final UserService userService;
    private final EventJpaRepository eventJpaRepository;

    /**
     * Создание комментария к событию
     *
     * @param userId     - id автора комментария
     * @param commentDto - DTO комментария
     * @return - DTO созданного комментария
     */
    @Transactional
    public CommentDto createComment(int userId, CommentDto commentDto) {
        User commentAuthor = UserMapper.toUser(userService.getUserById(userId)); //взяли из репозитория пользователя по id
        int eventId = commentDto.getEventId();
        Event event = eventJpaRepository.findById(eventId)
                .orElseThrow(() -> new ElementNotFoundException("Событие с id=" + eventId + " не найдено")); //взяли событие по id
        Comment comment = CommentMapper.toComment(commentDto, commentAuthor, event); //преобразовали все это в объект комментария
        Comment savedComment = commentJpaRepository.save(comment); //сохранили в репозиторий

        return CommentMapper.toDto(savedComment);
    }

    /**
     * Изменение комментария
     *
     * @param userId         - автор комментария
     * @param commentId      - id комментария
     * @param updatedComment - DTO Комментария с обновленными даннами
     * @return - DTO Обновленного комментария
     */
    @Transactional
    public CommentDto updateComment(int userId, int commentId, CommentDto updatedComment) {
        Comment comment = commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new ElementNotFoundException("Комментарий с Id=" + commentId + " не найден")); //взяли комментарий из репозитория

        if (comment.getAuthor().getId() != userId) {
            throw new BadParameterException("Пользователь с id=" + userId + "  не является автором комментария");
        }
        comment.setText(updatedComment.getText()); //сохранили в комментарии обновленный текст
        comment.setUpdated(LocalDateTime.now()); //поставили метку времени обновления комментария
        Comment savedComment = commentJpaRepository.save(comment); //сохранили в репозитории обновленный комментарий
        return CommentMapper.toDto(savedComment);
    }

    /**
     * Удаление комментария
     *
     * @param userId    - автор комментария
     * @param commentId - id комментария
     */
    @Transactional
    public void deleteComment(int userId, int commentId) {
        Comment comment = commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new ElementNotFoundException("Комментарий с Id=" + commentId + " не найден")); //взяли комментарий из репозитория

        if (comment.getAuthor().getId() != userId) {
            throw new BadParameterException("Пользователь с id=" + userId + "  не является автором комментария");
        }
        commentJpaRepository.deleteById(commentId); //удалили из репозитория комментарий
    }

    /**
     * Удаление комментария администратором
     *
     * @param commentId - id комментария
     */
    @Transactional
    public void deleteCommentByAdmin(int commentId) {
        Comment comment = commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new ElementNotFoundException("Комментарий с Id=" + commentId + " не найден")); //взяли комментарий из репозитория

        commentJpaRepository.deleteById(commentId); //удалили из репозитория комментарий
    }

    /**
     * Получение комментария по id
     *
     * @param commentId - id
     * @return - DTO комментария
     */
    public CommentDto getComment(int commentId) {
        Comment comment = commentJpaRepository.findById(commentId)
                .orElseThrow(() -> new ElementNotFoundException("Комментарий с id=" + commentId + " не найден")); //взяли коммент из репозитория
        return CommentMapper.toDto(comment);
    }
}
