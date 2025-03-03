package com.example.projektopgave1.CustomExceptions;

import java.time.LocalDateTime;

public class TimeSlotUnavailableException extends Exception {
    private LocalDateTime requestedStartTime;
    private LocalDateTime requestedEndTime;
    private String reason;

    public TimeSlotUnavailableException(String message) {
        super(message);
    }

    public TimeSlotUnavailableException(String message, LocalDateTime requestedStartTime, LocalDateTime requestedEndTime) {
        super(message);
        this.requestedStartTime = requestedStartTime;
        this.requestedEndTime = requestedEndTime;
    }

    public TimeSlotUnavailableException(String message, LocalDateTime requestedStartTime, LocalDateTime requestedEndTime, String reason) {
        super(message);
        this.requestedStartTime = requestedStartTime;
        this.requestedEndTime = requestedEndTime;
        this.reason = reason;
    }

    public TimeSlotUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocalDateTime getRequestedStartTime() {
        return requestedStartTime;
    }

    public LocalDateTime getRequestedEndTime() {
        return requestedEndTime;
    }

    public String getReason() {
        return reason;
    }
}