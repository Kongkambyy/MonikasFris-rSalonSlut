package com.example.projektopgave1.CustomExceptions;

public class InvalidInputException extends Exception {
    private String fieldName;
    private String invalidValue;
    private String expectedFormat;

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public InvalidInputException(String message, String fieldName, String invalidValue) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    public InvalidInputException(String message, String fieldName, String invalidValue, String expectedFormat) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
        this.expectedFormat = expectedFormat;
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getInvalidValue() {
        return invalidValue;
    }

    public String getExpectedFormat() {
        return expectedFormat;
    }
}