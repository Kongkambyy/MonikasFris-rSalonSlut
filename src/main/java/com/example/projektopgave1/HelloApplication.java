package com.example.projektopgave1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/projektopgave1/KalenderOversigt.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);

        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/saks.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Kunne ikke indlæse app-ikon: " + e.getMessage());
        }

        stage.setTitle("Monikas Frisørsalon - Kalendersystem");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}