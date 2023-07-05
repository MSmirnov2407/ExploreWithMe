package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserMapper;
import ru.practicum.exception.AlreadyExistException;
import ru.practicum.exception.BadParameterException;
import ru.practicum.exception.ElementNotFoundException;
import ru.practicum.exception.PaginationParametersException;
import ru.practicum.model.User;
import ru.practicum.repository.UserJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserJpaRepository userJpaRepository;

    @Autowired
    public UserService(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    /**
     * Добавление нового пользователя
     *
     * @param newUserRequest - DTO NewUserRequest с данными о пользователе
     * @return - в случае успешного сохранения возвращается UserDto
     */
    public UserDto createUser(NewUserRequest newUserRequest) {
        /*проверки перед добавлением*/
        String name = newUserRequest.getName();
        if (name == null || name.isBlank()) {
            throw new BadParameterException("Поле name не может быть пустым");
        }
        String newEmail = newUserRequest.getEmail();
        if (newEmail == null || newEmail.isBlank()) {
            throw new BadParameterException("Поле email не может быть пустым");
        }
        User testUser = userJpaRepository.findByEmail(newEmail);
        if (testUser != null) {
            throw new AlreadyExistException("Пользователь с email=" + newEmail + " уже существует");
        }
        /*добавление пользователя*/
        User user = userJpaRepository.save(UserMapper.toUser(newUserRequest));
        return UserMapper.toDto(user);
    }

    /**
     * Получение списка пользователей.
     * Пользователи запрашиваются либо согласно списка id, либо все пользователи
     * с учетом параметров пагиниации
     *
     * @param ids  - список id пользователей. Если список не передается, то учитываются параметры пагинации
     * @param from - параметр пагинации - с какого элемента выводить
     * @param size - параметр пагинации - сколько эл-ов выводить
     * @return - список DTO
     */
    public List<UserDto> getUsers(List<Integer> ids, Integer from, Integer size) {
        List<User> users; //список пользователей
        if (ids == null || ids.isEmpty()) { //если список id пустой, то запрашиваем согласно пагиинации
            if (from == null || size == null || from < 0 || size < 1) { //проверка параметров запроса
                throw new PaginationParametersException("Параметры постраничной выбрки должны быть from >=0, size >0");
            }
            PageRequest page = PageRequest.of(from / size, size, Sort.by("id").ascending()); //параметризируем переменную для пагинации
            users = userJpaRepository.findAll(page).getContent();
        } else { //если список id не пуст, то выгружаем пользователей согласно списка id
            users = userJpaRepository.findAllByIdIn(ids);
        }

        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение списка всех пользователей по списку id
     *
     * @param ids - список id пользователей.
     * @return - список DTO
     */
    public List<UserDto> getAllUsers(List<Integer> ids) {
        List<User> users; //список пользователей
        if (ids == null || ids.isEmpty()) { //если список id пустой, то Возвращаем пустой списко
            return new ArrayList<>();
        } else {
            users = userJpaRepository.findAllByIdIn(ids);
        }
        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * УДаление польхователя по id
     *
     * @param userId - id пользователя
     */
    public void deleteById(int userId) {
        Optional<User> userOptional = userJpaRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ElementNotFoundException("Пользователь с id= " + userId + " не найден");
        }
        userJpaRepository.deleteById(userId);
    }

    /**
     * Получение пользователя по id
     *
     * @param userId - id искомого пользователя
     * @return - DTO пользователя
     */
    public UserDto getUserById(int userId) {
        Optional<User> userOptional = userJpaRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new ElementNotFoundException("Пользователь с id= " + userId + " не найден");
        }
        return UserMapper.toDto(userOptional.get());
    }

}
