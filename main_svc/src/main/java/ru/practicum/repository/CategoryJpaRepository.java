package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Category;

public interface CategoryJpaRepository extends JpaRepository<Category, Integer> {
    Category findByName(String name);
}
