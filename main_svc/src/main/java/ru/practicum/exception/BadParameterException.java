package ru.practicum.exception;

public class BadParameterException extends RuntimeException {
    public BadParameterException(String mess) {
        super(mess);
    }
}
