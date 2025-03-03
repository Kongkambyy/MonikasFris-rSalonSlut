package com.example.projektopgave1.CustomExceptions;

public class HairdresserBusyException extends Exception {
  private int medarbejderID;
  private String requestedTimeSlot;

  public HairdresserBusyException(String message) {
    super(message);
  }

  public HairdresserBusyException(String message, int medarbejderID, String requestedTimeSlot) {
    super(message);
    this.medarbejderID = medarbejderID;
    this.requestedTimeSlot = requestedTimeSlot;
  }

  public HairdresserBusyException(String message, Throwable cause) {
    super(message, cause);
  }

  public int getMedarbejderID() {
    return medarbejderID;
  }

  public String getRequestedTimeSlot() {
    return requestedTimeSlot;
  }
}