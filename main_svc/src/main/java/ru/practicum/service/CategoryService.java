package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dto.categoty.CategoryDto;
import ru.practicum.dto.categoty.CategoryMapper;
import ru.practicum.dto.categoty.NewCategoryDto;
import ru.practicum.exception.AlreadyExistException;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.exception.PaginationParametersException;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryJpaRepository categoryJpaRepository;
    //todo del
//    private final EventService eventService;

    @Autowired
    public CategoryService(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
        //todo del
//        this.eventService = eventService;
    }

    /**
     * Создание категории
     *
     * @param newCategoryDto - DTO новой категории
     * @return - DTO созданной Категории
     */
    public CategoryDto create(NewCategoryDto newCategoryDto) {
        /*проверка параметров*/
        String name = newCategoryDto.getName();
        if (name == null || name.isBlank()) {
            throw new BadParameterException("Имя категории не должно быть пустым");
        }
        if (categoryJpaRepository.findByName(name) != null) {  //ищем в репозитории категорию с заданным именем
            throw new AlreadyExistException("Категория с именем " + name + " уже существует");
        }

        Category category = categoryJpaRepository.save(CategoryMapper.toCategory(newCategoryDto)); //сохранение в репозитоорий
        return CategoryMapper.toDto(category); //вернули DTO категории
    }

    /**
     * УДаление категории по id
     *
     * @param catId - id категории
     */
    public void deleteById(int catId) {
        Optional<Category> categoryOptional = categoryJpaRepository.findById(catId);
        if (categoryOptional.isEmpty()) {
            throw new ElementNotFoundException("Категория с id= " + catId + " не найдена");
        }
        //todo удалить. Ограничение сделано в schema.sql ON DELETE RESTRICT
//        if (!eventService.getEventsByCategory(catId).isEmpty()) {
//            throw new DataConflictException("Нельзя удалить категорию, т.к. с ней связаны события");
//        }
        categoryJpaRepository.deleteById(catId);
    }

    /**
     * Обновление категории
     *
     * @param categoryDto - DTO с обновляемыми данным
     * @return - обновленный DTO
     */
    public CategoryDto updateCategory(int catId, CategoryDto categoryDto) {
        /*проверка параметров*/
        String name = categoryDto.getName();
        if (name.isBlank()) {
            throw new BadParameterException("Имя категории должно быть не пустым");
        }
        Optional<Category> categoryOptional = categoryJpaRepository.findById(catId); //запросили категорию по id
        if (categoryOptional.isEmpty()) {
            throw new ElementNotFoundException("категории с id=" + catId + " не существует");
        }
        Category category = categoryOptional.get();
        if (category.getName().equals(name)) { //если имя не изменилось то возвращаем данные без обращения к БД, т.к. других полей нет
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
        if (from < 0 || size < 1) { //проверка параметров запроса
            throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
        }
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
//todo del
        System.out.println("CategoryService getCatById= " + categoryId);

        /*проверка параметров запроса*/
        if (categoryId <= 0) {
            throw new BadParameterException("Id не может быть меньше 1");
        }
        /*запрос категории из репозитория*/
        Optional<Category> categoryOptional = categoryJpaRepository.findById(categoryId);
        /*проверка наличия искомого элемента*/
        if (categoryOptional.isEmpty()) {
            throw new ElementNotFoundException("Элемент с id=" + categoryId + " не найден");
        }
        return CategoryMapper.toDto(categoryOptional.get());
    }

}
