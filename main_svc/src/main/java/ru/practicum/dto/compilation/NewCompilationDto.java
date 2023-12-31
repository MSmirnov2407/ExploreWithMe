package ru.practicum.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Подборка событий
 */
@Getter
@Setter
@AllArgsConstructor
public class NewCompilationDto {
    private Set<Integer> events; //списко Id событий
    private boolean pinned; //закреплена ли подборка на главной странице

    @NotNull
    @NotBlank
    @Size(min = 1, max = 50)
    private String title; //название подборки

//    public NewCompilationDto() {
//        this.events = new HashSet<>();
//        this.pinned = false;
//        this.title = "";
//    }
}
