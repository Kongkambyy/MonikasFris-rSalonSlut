package com.example.projektopgave1.CustomExceptions;

public class BookingNotPossibleException extends Exception {
    private String reason;

    public BookingNotPossibleException(String message) {
        super(message);
    }

    public BookingNotPossibleException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public BookingNotPossibleException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getReason() {
        return reason;
    }
}