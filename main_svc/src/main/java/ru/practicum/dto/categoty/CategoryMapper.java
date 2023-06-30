package ru.practicum.dto.categoty;

import ru.practicum.model.Category;

/**
 * Класс, содержащий статические методоы для преобразования объекта Category в его DTO и обратно
 */
public class CategoryMapper {

    /**
     * Преобразование объекта Category в CategoryDto
     *
     * @param category - объект категории
     * @return - DTO
     */
    public static CategoryDto toDto(Category category) {
        CategoryDto categoryDto = new CategoryDto();

        /*заполняем поля DTO значениями из объекта*/
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());

        return categoryDto;
    }

    /**
     * Преобразование объекта NewCategoryDto в CategoryDto
     *
     * @param newCategoryDto - DTO  NewCategoryDto
     * @return - DTO CategoryDto
     */
    public static CategoryDto toDto(NewCategoryDto newCategoryDto) {
        CategoryDto categoryDto = new CategoryDto();

        /*заполняем поля DTO значениями из объекта*/
        categoryDto.setName(newCategoryDto.getName());

        return categoryDto;
    }

    /**
     * Преобразование объекта NewCategoryDto в Category
     *
     * @param categoryDto - DTO новой категории
     * @return - категория
     */
    public static Category toCategory(NewCategoryDto categoryDto) {
        Category category = new Category();

        /*заполняем поля Объекта значениями из DTO*/
        category.setName(categoryDto.getName());

        return category;
    }

    /**
     * Преобразование объекта CategoryDto в Category
     *
     * @param categoryDto - DTO  категории
     * @return - категория
     */
    public static Category toCategory(CategoryDto categoryDto) {
        Category category = new Category();

        /*заполняем поля Объекта значениями из DTO*/
        category.setName(categoryDto.getName());

        return category;
    }
}
