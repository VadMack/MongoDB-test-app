package com.vadmack.mongodbtest.exception;

public class ServerSideException extends RuntimeException{
    public ServerSideException(String message) {
        super(message);
    }
}
