package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.categoty.CategoryDto;
import ru.practicum.dto.categoty.CategoryMapper;
import ru.practicum.dto.categoty.NewCategoryDto;
import ru.practicum.exception.AlreadyExistException;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryJpaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryJpaRepository categoryJpaRepository;

    /**
     * Создание категории
     *
     * @param newCategoryDto - DTO новой категории
     * @return - DTO созданной Категории
     */
    @Transactional
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        /*проверка параметров*/
        String name = newCategoryDto.getName();
        if (categoryJpaRepository.findByName(name) != null) {  //ищем в репозитории категорию с заданным именем
            throw new AlreadyExistException("Категория с именем " + name + " уже существует");
        }

        Category category = categoryJpaRepository.save(CategoryMapper.toCategory(newCategoryDto)); //сохранение в репозитоорий
        return CategoryMapper.toDto(category); //вернули DTO категории
    }

    /**
     * Удаление категории по id
     *
     * @param catId - id категории
     */
    public void deleteById(int catId) {
        categoryJpaRepository.findById(catId)
                .orElseThrow(() -> new ElementNotFoundException("Категория с id= " + catId + " не найдена")); //проверка существования категории
        try {
            categoryJpaRepository.deleteById(catId);
        } catch (RuntimeException ex) {
            throw new DataConflictException("Не удалось удалить категорию. Возможно, существуют связанные события");
        }
    }

    /**
     * Обновление категории
     *
     * @param categoryDto - DTO с обновляемыми данным
     * @return - обновленный DTO
     */
    @Transactional
    public CategoryDto updateCategory(int catId, CategoryDto categoryDto) {
        String name = categoryDto.getName();

        Category category = categoryJpaRepository.findById(catId)
                .orElseThrow(() -> new ElementNotFoundException("категории с id=" + catId + " не существует")); //запросили категорию по id
        if (category.getName().equals(name)) { // если имя не изменилось, то возвращаем данные без обращения к БД, т.к. других полей нет
            return CategoryMapper.toDto(category);
        }
        if (categoryJpaRepository.findByName(name) != null) { //если имя не совпало, то проверяем, пересекается ли новое имя с другими
            throw new AlreadyExistException("категория с таким именем уже существует");
        }
        category.setName(categoryDto.getName()); //обновили имя категории
        categoryJpaRepository.save(category); //сохранили обновленную категорию
        return CategoryMapper.toDto(category);
    }


    /**
     * Получение категорий из репозитория
     *
     * @param from - параметр пагинации - с какого элемента выводить
     * @param size - параметр пагинации - сколько эл-ов выводить
     * @return - список DTO категорий
     */
    public List<CategoryDto> getAllCategories(int from, int size) {
        PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending()); //параметризируем переменную для пагинации

        List<Category> categories = categoryJpaRepository.findAll(page).getContent(); //берем из репозитория нужные категории
        /*преобразование List<Category> в List<CategoryDto>*/
        return categories.stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList()); //преобразуем в список DTO и возвращаем
    }

    /**
     * Получение категориипо id
     *
     * @param categoryId - id категории
     * @return - DTO категории
     */
    public CategoryDto getCategoryById(int categoryId) {
        /*проверка параметров запроса*/
        if (categoryId <= 0) {
            throw new BadParameterException("Id не может быть меньше 1");
        }

        Category category = categoryJpaRepository.findById(categoryId)
                .orElseThrow(() -> new ElementNotFoundException("Элемент с id=" + categoryId + " не найден")); //берем категорию из репозтороя

        return CategoryMapper.toDto(category);
    }

}
