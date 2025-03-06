package com.example.projektopgave1.Controller;

import com.example.projektopgave1.Model.UseCases.UseCaseLogin;
import com.example.projektopgave1.Model.Entiteter.Medarbejder;
import Utils.LoggerUtility;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class LoginMenuController {

    // UI elementer fra FXML filen som vi skal bruge
    @FXML private Label welcomeLabel;
    @FXML private TextField usernameTextField;
    @FXML private TextField passwordTextField;
    @FXML private Button loginButton;
    @FXML private ImageView salonImageView;

    // Use case til login som vi bruger til at tjekke login oplysninger
    private UseCaseLogin loginUseCase;


    // Dette køre så snart .fxml er loadet for brugeren.
    @FXML
    public void initialize() {
        try {
            // Opret loginUseCase så vi kan tjekke login
            loginUseCase = new UseCaseLogin();

            // Indlæs salon billedet til højre i vinduet
            indlaesSalonBillede();

            LoggerUtility.logEvent("Login controller startet");
        } catch (Exception e) {
            LoggerUtility.logError("Fejl i login controller: " + e.getMessage());
        }
    }

    // ChatGPT har hjulpet med denne metode, da vi ikke kunne initialisere billedet gennem .fxml
    private void indlaesSalonBillede() {
        try {
            String[] muligeBilledStier = {
                    "/images/MonikaBillede.png",
                    "/images/MonikaBillede.jpg",
                    "images/MonikaBillede.png",
                    "images/MonikaBillede.jpg"
            };

            Image billede = null;
            for (String sti : muligeBilledStier) {
                try {
                    var billedeStream = getClass().getResourceAsStream(sti);
                    if (billedeStream != null) {
                        billede = new Image(billedeStream);
                        LoggerUtility.logEvent("Fandt billedet på stien: " + sti);
                        break;
                    }

                    billede = new Image(getClass().getResource(sti).toExternalForm());
                    if (billede != null && !billede.isError()) {
                        LoggerUtility.logEvent("Fandt billedet som URL: " + sti);
                        break;
                    }
                } catch (Exception e) {
                    LoggerUtility.logWarning("Kunne ikke loade billede fra: " + sti);
                }
            }

            if (billede != null && !billede.isError()) {
                salonImageView.setImage(billede);
            } else {
                LoggerUtility.logWarning("Kunne ikke finde salon billedet nogen steder");
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved indlæsning af billede: " + e.getMessage());
        }
    }

    // Her tjekker vi brugerens input, og ser om det stemmer
    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            LoggerUtility.logEvent("Login knap klikket");

            // Hent brugerens input
            String brugernavn = usernameTextField.getText();
            String kodeord = passwordTextField.getText();

            // Tjek at brugernavn er udfyldt
            if (brugernavn == null || brugernavn.trim().isEmpty()) {
                visAlert(Alert.AlertType.ERROR, "Login Fejl", "Indtast venligst et brugernavn");
                return;
            }

            // Tjek at kodeord er udfyldt
            if (kodeord == null || kodeord.trim().isEmpty()) {
                visAlert(Alert.AlertType.ERROR, "Login Fejl", "Indtast venligst et kodeord");
                return;
            }

            // Prøv at logge ind med de indtastede oplysninger
            Medarbejder loggetIndMedarbejder = loginUseCase.authenticateUser(brugernavn, kodeord);

            if (loggetIndMedarbejder != null) {
                // Hvis login er ok, kalder vi næste metode som går til kalendervisningen
                gaaTilKalender();
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl under login: " + e.getMessage());
            visAlert(
                    Alert.AlertType.ERROR,
                    "System Fejl",
                    "Der skete en uventet fejl. Prøv igen senere."
            );
        }
    }

     // Skifter til kalendervisningen efter login
    private void gaaTilKalender() {
        try {
            // Indlæs kalender visningen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projektopgave1/KalenderOversigt.fxml"));
            Parent kalenderRoot = loader.load();

            // Find det nuværende vindue
            Stage nuværendeVindue = (Stage) loginButton.getScene().getWindow();

            // Opreter og sætter den nye scene
            Scene kalenderScene = new Scene(kalenderRoot);
            nuværendeVindue.setScene(kalenderScene);
            nuværendeVindue.setTitle("Monika's Frisørsalon - Kalender");

            LoggerUtility.logEvent("Skiftet til kalender visning");
        } catch (Exception e) {
            LoggerUtility.logError("Kunne ikke skifte til kalender: " + e.getMessage());
            visAlert(
                    Alert.AlertType.ERROR,
                    "Navigations Fejl",
                    "Kunne ikke åbne kalender visningen. Prøv igen."
            );
        }
    }

     // Viser en alert med den givne type, titel og besked
    private void visAlert(Alert.AlertType type, String titel, String besked) {
        Alert alert = new Alert(type);
        alert.setTitle(titel);
        alert.setHeaderText(null);
        alert.setContentText(besked);
        alert.showAndWait();
    }
}