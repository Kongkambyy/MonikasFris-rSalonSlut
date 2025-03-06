package com.example.projektopgave1;

import Utils.LoggerUtility;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/projektopgave1/LoginMenu.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 779, 589);

            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/saks.png"));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                LoggerUtility.logWarning("Kunne ikke indlæse app-ikon: " + e.getMessage());
            }

            stage.setTitle("Monikas Frisørsalon - Login");
            stage.setScene(scene);
            stage.setMinWidth(700);
            stage.setMinHeight(500);
            stage.show();

            LoggerUtility.logEvent("Applikation startet");
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved opstart af applikation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        LoggerUtility.logEvent("Applikation lukket");
    }
}