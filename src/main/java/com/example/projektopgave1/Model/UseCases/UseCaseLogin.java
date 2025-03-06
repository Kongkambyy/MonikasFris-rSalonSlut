package com.example.projektopgave1.Model.UseCases;

import com.example.projektopgave1.Model.Entiteter.Medarbejder;
import com.example.projektopgave1.CustomExceptions.DatabaseConnectionException;
import com.example.projektopgave1.Model.DatabaseHandlers.MedarbejderDatabaseHandler;

import Utils.LoggerUtility;

public class UseCaseLogin {
    private final MedarbejderDatabaseHandler medarbejderDatabaseHandler;

    public UseCaseLogin() {
        this.medarbejderDatabaseHandler = new MedarbejderDatabaseHandler();
    }

    public Medarbejder login(String username, String password) {
        try {
            for (Medarbejder medarbejder : medarbejderDatabaseHandler.getAll()) {
                if (medarbejder.getBrugernavn().equals(username) && medarbejder.getAdgangskode().equals(password)) {
                    LoggerUtility.logEvent("Succesfuldt login for: " + username);
                    return medarbejder;
                }
            }
        } catch (DatabaseConnectionException e) {
            LoggerUtility.logError("Databasefejl ved login: " + e.getMessage());
            return null;
        }
        return null;
    }
}