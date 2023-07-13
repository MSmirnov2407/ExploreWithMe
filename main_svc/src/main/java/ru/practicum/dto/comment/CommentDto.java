package ru.practicum.dto.comment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class CommentDto {
    private int id;
    @NotBlank
    @Size(max = 512)
    private String text;
    private String authorName;
    @NotNull
    @Positive
    private int eventId;
    LocalDateTime created = LocalDateTime.now();
    LocalDateTime updated = LocalDateTime.now();
}
