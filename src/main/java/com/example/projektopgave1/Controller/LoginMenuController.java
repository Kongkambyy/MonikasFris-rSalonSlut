package com.example.projektopgave1.Controller;

import Utils.LoggerUtility;
import com.example.projektopgave1.Model.Entiteter.Medarbejder;
import com.example.projektopgave1.Model.UseCases.UseCaseLogin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;

public class LoginMenuController {

    @FXML private Label welcomeLabel;
    @FXML private TextField usernameTextField;
    @FXML private TextField passwordTextField;
    @FXML private Button loginButton;

    private UseCaseLogin useCaseLogin;

    @FXML
    public void initialize() {
        useCaseLogin = new UseCaseLogin();
        LoggerUtility.logEvent("LoginMenuController initialiseret");

        if (loginButton == null) {
            LoggerUtility.logError("Login-knap blev ikke indlæst korrekt via FXMLLoader");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        LoggerUtility.logEvent("Login-knap klikket");

        if (usernameTextField == null || passwordTextField == null) {
            LoggerUtility.logError("Tekstfelter blev ikke indlæst korrekt");
            return;
        }

        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            LoggerUtility.logWarning("Brugernavn eller adgangskode er tom");
            return;
        }

        LoggerUtility.logEvent("Forsøger login med brugernavn: " + username);

        Medarbejder medarbejder = useCaseLogin.login(username, password);
        if (medarbejder != null) {
            LoggerUtility.logEvent("Succesfuldt login for: " + username);
            navigateToCalendar();
        } else {
            LoggerUtility.logError("Fejlet loginforsøg for: " + username);
        }
    }

    private void navigateToCalendar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projektopgave1/KalenderOversigt.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            LoggerUtility.logError("Fejl ved navigation til kalenderoversigt: " + e.getMessage());
            e.printStackTrace();
        }
    }
}