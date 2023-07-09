package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @ExceptionHandler({PaginationParametersException.class, BadParameterException.class})
    public ResponseEntity<ApiError> handlePaginationException(RuntimeException ex) {
        ApiError apiError = new ApiError();
        /*получаем стектрейсы*/
        List<String> stackTraceList = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());
        /*заполняем остальные поля ответа*/
        apiError.setErrors(stackTraceList);
        apiError.setReason(HttpStatus.BAD_REQUEST.getReasonPhrase());
        apiError.setTimeStamp(LocalDateTime.now().format(TIME_FORMAT));
        apiError.setStatus(HttpStatus.BAD_REQUEST.name());
        apiError.setMessage(ex.getMessage());
        log.debug(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST); //400
    }

    @ExceptionHandler({ElementNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ApiError> handleElementNotFoundException(RuntimeException ex) {
        ApiError apiError = new ApiError();
        /*получаем стектрейсы*/
        List<String> stackTraceList = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());
        /*заполняем остальные поля ответа*/
        apiError.setErrors(stackTraceList);
        apiError.setReason(HttpStatus.NOT_FOUND.getReasonPhrase());
        apiError.setTimeStamp(LocalDateTime.now().format(TIME_FORMAT));
        apiError.setStatus(HttpStatus.NOT_FOUND.name());
        apiError.setMessage(ex.getMessage());
        log.debug(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND); //404
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<ApiError> handleAlreadyExistException(AlreadyExistException ex) {
        ApiError apiError = new ApiError();
        /*получаем стектрейсы*/
        List<String> stackTraceList = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());
        /*заполняем остальные поля ответа*/
        apiError.setErrors(stackTraceList);
        apiError.setReason(HttpStatus.CONFLICT.getReasonPhrase());
        apiError.setTimeStamp(LocalDateTime.now().format(TIME_FORMAT));
        apiError.setStatus(HttpStatus.CONFLICT.name());
        apiError.setMessage(ex.getMessage());
        log.debug(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT); //409
    }

    @ExceptionHandler({CreateConditionException.class, DataConflictException.class})
    public ResponseEntity<ApiError> handleCreateConditionException(RuntimeException ex) {
        ApiError apiError = new ApiError();
        /*получаем стектрейсы*/
        List<String> stackTraceList = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());
        /*заполняем остальные поля ответа*/
        apiError.setErrors(stackTraceList);
        apiError.setReason(HttpStatus.CONFLICT.getReasonPhrase());
        apiError.setTimeStamp(LocalDateTime.now().format(TIME_FORMAT));
        apiError.setStatus(HttpStatus.CONFLICT.name());
        apiError.setMessage(ex.getMessage());
        log.debug(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT); //409
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleAllAnother(RuntimeException ex) {
        ApiError apiError = new ApiError();
        /*получаем стектрейсы*/
        List<String> stackTraceList = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.toList());
        /*заполняем остальные поля ответа*/
        apiError.setErrors(stackTraceList);
        apiError.setReason(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        apiError.setTimeStamp(LocalDateTime.now().format(TIME_FORMAT));
        apiError.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
        apiError.setMessage(ex.getMessage());
        log.debug(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR); //500
    }
}
