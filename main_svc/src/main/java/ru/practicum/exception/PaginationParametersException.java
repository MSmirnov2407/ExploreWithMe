package ru.practicum.exception;

public class PaginationParametersException extends RuntimeException {
    public PaginationParametersException(String message) {
        super(message);
    }
}
