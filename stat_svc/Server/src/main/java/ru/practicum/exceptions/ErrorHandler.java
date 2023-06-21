package ru.practicum.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.UnsupportedEncodingException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler(UnsupportedEncodingException.class)
    public ResponseEntity<String> unsupportedEncodingHandle(UnsupportedEncodingException e) {
        log.info("UnsupportedEncodingException: {}", e.getMessage());
        return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST); //400
    }
}
