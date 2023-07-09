package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Compilation;

import java.util.List;

public interface CompilationJpaRepository extends JpaRepository<Compilation, Integer> {
    /**
     * Получение всех подборок, с учетом признака закрепленности на главной странице
     *
     * @param pinned - призгнак, закреплена ли подборка на главной странице
     * @param page   - параметры постраничного вывода
     * @return - список подборок
     */
    List<Compilation> findByPinned(boolean pinned, Pageable page);
}
