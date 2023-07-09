package ru.practicum.exception;

public class AlreadyExistException extends RuntimeException {
    public AlreadyExistException(String mess) {
        super(mess);
    }
}
