package ru.practicum.dto.user;

import ru.practicum.model.User;

/**
 * Класс, содержащий статические методоы для преобразования объекта Event в его DTO и обратно
 */
public class UserMapper {

    /**
     * Преобразование объекта User в UserShortDto
     *
     * @param user - объект
     * @return - DTO
     */
    public static UserShortDto toShortDto(User user) {
        UserShortDto shortDto = new UserShortDto();
        /*заполняем поля DTO значениями из объекта*/
        shortDto.setId(user.getId());
        shortDto.setName(user.getName());
        return shortDto;
    }

    /**
     * Преобразование объекта User в UserDto
     *
     * @param user - user
     * @return - UserDto
     */
    public static UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        /*заполняем поля DTO значениями из объекта*/
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    /**
     * Преобразование DTO NewUserRequest в объект User
     *
     * @param newUserRequest - DTO
     * @return - User
     */
    public static User toUser(NewUserRequest newUserRequest) {
        User user = new User();
        /*заполняем поля Объекта значениями из DTO*/
        user.setEmail(newUserRequest.getEmail());
        user.setName(newUserRequest.getName());
        return user;
    }

    /**
     * Преобразование DTO UserDto в объект User
     *
     * @param userDto - DTO
     * @return - User
     */
    public static User toUser(UserDto userDto) {
        User user = new User();
        /*заполняем поля Объекта значениями из DTO*/
        user.setId(userDto.getId());
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        return user;
    }
}
