package ru.practicum.dto.categoty;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Данные для добавления новой категории
 */
@Getter
@Setter
public class NewCategoryDto {
    @NotBlank
    @Size(min = 1, max = 50)
    private String name; //название категории
}
