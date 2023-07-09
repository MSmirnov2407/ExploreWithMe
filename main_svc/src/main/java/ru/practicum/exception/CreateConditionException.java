package ru.practicum.exception;

public class CreateConditionException extends RuntimeException {
    public CreateConditionException(String mess) {
        super(mess);
    }
}
