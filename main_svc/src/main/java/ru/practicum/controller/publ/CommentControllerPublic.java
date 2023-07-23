package ru.practicum.controller.publ;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.service.CommentService;

import javax.validation.constraints.Positive;

@RestController
@RequestMapping(path = "/comments")
@Slf4j
@RequiredArgsConstructor
@Validated
public class CommentControllerPublic {
    private final CommentService commentService;

    /**
     * Получение комментария по id
     *
     * @param commentId - id
     * @return - DTO Комментария
     */
    @GetMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK) //200
    public CommentDto getComment(@PathVariable(name = "commentId") @Positive int commentId) {
        CommentDto commentDto = commentService.getComment(commentId);
        log.info("Получен комментарий с id={}", commentId);
        return commentDto;
    }
}
