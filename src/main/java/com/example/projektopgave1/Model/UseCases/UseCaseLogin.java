package com.example.projektopgave1.Model.UseCases;

import com.example.projektopgave1.CustomExceptions.DatabaseConnectionException;
import com.example.projektopgave1.Model.DatabaseHandlers.MedarbejderDatabaseHandler;
import com.example.projektopgave1.Model.Entiteter.Medarbejder;
import Utils.LoggerUtility;

public class UseCaseLogin {

    private MedarbejderDatabaseHandler medarbejderDB;

    // Opretter databaseobjektet
    public UseCaseLogin() {
        this.medarbejderDB = new MedarbejderDatabaseHandler();
        LoggerUtility.logEvent("UseCaseLogin initialiseret");
    }

    // Her laver vi tjekket om medarbejderen er oprettet i databasen
    public Medarbejder authenticateUser(String username, String password) {
        LoggerUtility.logEvent("Bruger: " + username + "forsøger at logge ind");

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            LoggerUtility.logWarning("Indtastet tomme felter");
            return null;
        }

        // Her kører vi et simpelt tjek, om brugernavn og password matcher
        try {
            var allEmployees = medarbejderDB.getAll();

            for (Medarbejder employee : allEmployees) {
                if (employee.getBrugernavn().equals(username) &&
                        employee.getAdgangskode().equals(password)) {

                    LoggerUtility.logEvent("Brugeren: " + username + "er logget ind");
                    return employee;
                }
            }

            LoggerUtility.logWarning("Brugeren: " + username + " kunne ikke logge ind. Forkerte informationer");
            return null;

        } catch (DatabaseConnectionException e) {
            LoggerUtility.logError("Databasefejl: " + e.getMessage());
            return null;
        } catch (Exception e) {
            LoggerUtility.logError("Fejl i verificeringen af medarbejderen: " + e.getMessage());
            return null;
        }
    }

    public boolean userExists(String username) {
        try {
            var allEmployees = medarbejderDB.getAll();

            for (Medarbejder employee : allEmployees) {
                if (employee.getBrugernavn().equals(username)) {
                    return true;
                }
            }

            return false;
        } catch (DatabaseConnectionException e) {
            LoggerUtility.logError("Fejl i at hente medarbejderen: " + e.getMessage());
            return false;
        }
    }
}