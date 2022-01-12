package com.vadmack.mongodbtest.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message){
        super(message);
    }
}
