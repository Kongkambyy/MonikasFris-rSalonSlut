package com.example.projektopgave1.CustomExceptions;

public class DatabaseConnectionException extends Exception {
  private String connectionDetails;

  public DatabaseConnectionException(String message) {
    super(message);
  }

  public DatabaseConnectionException(String message, String connectionDetails) {
    super(message);
    this.connectionDetails = connectionDetails;
  }

  public DatabaseConnectionException(String message, Throwable cause) {
    super(message, cause);
  }

  public DatabaseConnectionException(String message, String connectionDetails, Throwable cause) {
    super(message, cause);
    this.connectionDetails = connectionDetails;
  }

  public String getConnectionDetails() {
    return connectionDetails;
  }
}