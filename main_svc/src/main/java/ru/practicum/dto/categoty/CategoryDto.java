package ru.practicum.dto.categoty;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Категория событий
 */
@Getter
@Setter
public class CategoryDto {

    private int id; // id категории
    @NotBlank
    @Size(min = 1, max = 50)
    private String name; //название категории
}
