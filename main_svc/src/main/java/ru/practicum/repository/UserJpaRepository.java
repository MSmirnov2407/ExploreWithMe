package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.User;

import java.util.List;

public interface UserJpaRepository extends JpaRepository<User, Integer> {

    User findByEmail(String email);

    List<User> findAllByIdIn(int[] ids);
}
