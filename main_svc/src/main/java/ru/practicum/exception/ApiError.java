package ru.practicum.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Сведения об ошибке
 */
@Getter
@Setter
public class ApiError {
    private List<String> errors; //список стектрейсов или описания ошибок
    private String message; //Сообщение об ошибке
    private String reason; //описание причины ошибки
    private String status;//Код статуса HTTP-ответа
    private String timeStamp; //Дата и время когда произошла ошибка (в формате "yyyy-MM-dd HH:mm:ss")
}
