package com.amp.mongodeploy;

public class MongoDeployException extends RuntimeException {
    public MongoDeployException(String message, Exception cause) {
        super(message, cause);
    }
}
