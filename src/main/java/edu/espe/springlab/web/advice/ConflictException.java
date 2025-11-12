package edu.espe.springlab.web.advice;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
