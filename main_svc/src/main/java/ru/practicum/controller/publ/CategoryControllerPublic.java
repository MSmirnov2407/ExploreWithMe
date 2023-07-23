package ru.practicum.controller.publ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.categoty.CategoryDto;
import ru.practicum.service.CategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/categories")
@Slf4j
@Validated
public class CategoryControllerPublic {

    private final CategoryService categoryService;

    @Autowired
    public CategoryControllerPublic(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    /**
     * Получение списка категорий с учетом пагинации
     *
     * @param from - параметр пагинации - с какого элемента выводить
     * @param size - параметр пагинации - сколько эл-ов выводить
     * @return - список DTO категорий
     */
    @GetMapping()
    @ResponseStatus(HttpStatus.OK) //200
    public List<CategoryDto> getCategories(@RequestParam(name = "from", defaultValue = "0") @PositiveOrZero int from,
                                           @RequestParam(name = "size", defaultValue = "10") @Positive int size) {
        List<CategoryDto> categoryDtos = categoryService.getAllCategories(from, size);
        log.info("Получен список всех категорий через Публичный контроллер");
        return categoryDtos;
    }

    /**
     * получение категории событий по ее id
     */
    @GetMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.OK) //200
    public CategoryDto getCategory(@PathVariable int categoryId) {
        CategoryDto categoryDto = categoryService.getCategoryById(categoryId);
        log.info("Получена категория с id={} через Публичный контроллер", categoryId);
        return categoryDto;
    }
}

