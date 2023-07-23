package ru.practicum.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@Slf4j
@Validated
public class UserControllerAdmin {
    private final UserService userService;

    @Autowired
    public UserControllerAdmin(UserService userService) {
        this.userService = userService;
    }

    /**
     * Создание пользователя
     *
     * @param newUserRequest - DTO нового пользователя
     * @return - DTO созданного пользователя
     */
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED) //201
    public UserDto postUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        UserDto userDto = userService.createUser(newUserRequest);
        log.info("Создан новый пользователь name={}, email={}", userDto.getName(), userDto.getEmail());

        return userDto;
    }

    /**
     * Получение списка пользователей по списку id или всех пользователей постранично
     *
     * @param ids  - список id. Если список не передается, то учитываются параметры пагинации
     * @param from - параметр пагинации - с какого элемента выводить
     * @param size - параметр пагинации - сколько эл-ов выводить
     * @return - список DTO пользователей
     */
    @GetMapping
    public List<UserDto> getAllUsers(@RequestParam(name = "ids", required = false) List<Integer> ids,
                                     @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                     @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Запрошен список пользователей. ids={}, from={}, size={}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    /**
     * @param userId
     */
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteUser(@PathVariable(name = "userId") int userId) {
        userService.deleteById(userId);
        log.info("Удален пользователь с Id={}", userId);
    }
}
