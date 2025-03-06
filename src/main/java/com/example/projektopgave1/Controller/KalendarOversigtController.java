package com.example.projektopgave1.Controller;

import com.example.projektopgave1.Model.UseCases.UseCaseCalendar;
import com.example.projektopgave1.Model.UseCases.UseCaseCalendar.AppointmentData;
import Utils.LoggerUtility;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class KalendarOversigtController {

    @FXML private Button todayButton;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label currentDateLabel;
    @FXML private ComboBox<String> customerBookingsComboBox;
    @FXML private ToggleButton dayToggle;
    @FXML private ToggleButton weekToggle;
    @FXML private ToggleButton monthToggle;
    @FXML private ToggleButton yearToggle;
    @FXML private CheckBox allBookingsCheckBox;
    @FXML private CheckBox jonCheckBox;
    @FXML private CheckBox joachimCheckBox;
    @FXML private CheckBox lasseCheckBox;
    @FXML private CheckBox gabrielCheckBox;
    @FXML private Button newBookingButton;
    @FXML private Button editButton;
    @FXML private Button cancelBookingButton;
    @FXML private GridPane calendarGrid;
    private VBox selectedAppointment = null;

    private UseCaseCalendar useCaseCalendar;

    @FXML
    public void initialize() {
        try {
            // Opretter use case objektet
            useCaseCalendar = new UseCaseCalendar();

            // Opdatere datoen der vises
            opdaterDatoLabel();

            // Sætter knapper og events op
            sætEventHandlersOp();

            // Sætter UI op så det er klar til brug
            sætStartTilstandOp();

            // Opretter kalender grid med felter til hver dag/tid
            opsætKalenderGrid();

            // Indlæser bookingerne og vis dem
            genindlæsKalender();

            LoggerUtility.logEvent("Kalenderoversigtvisning initialiseret");
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved initialisering af kalenderoversigt: " + e.getMessage());
        }
    }


    // Event handlers
    private void sætEventHandlersOp() {
        // I dag knappen går til dags dato
        todayButton.setOnAction(event -> {
            useCaseCalendar.moveToToday();
            opdaterDatoLabel();
            genindlæsKalender();
        });

        // Tilbage knappen går en uge tilbage
        prevButton.setOnAction(event -> {
            useCaseCalendar.moveToPreviousWeek();
            opdaterDatoLabel();
            genindlæsKalender();
        });

        // Frem knappen går en uge frem
        nextButton.setOnAction(event -> {
            useCaseCalendar.moveToNextWeek();
            opdaterDatoLabel();
            genindlæsKalender();
        });

        // Alle bookinger checkboxen styrer de andre checkboxes
        allBookingsCheckBox.setOnAction(event -> {
            opdaterMedarbejderCheckboxTilstand();
            genindlæsKalender();
        });

        // Medarbejder checkboxes opdaterer visningen når de ændres
        jonCheckBox.setOnAction(event -> genindlæsKalender());
        joachimCheckBox.setOnAction(event -> genindlæsKalender());
        lasseCheckBox.setOnAction(event -> genindlæsKalender());
        gabrielCheckBox.setOnAction(event -> genindlæsKalender());

        // Knapper til at lave nye bookings eller ændre eksisterende
        newBookingButton.setOnAction(event -> håndterNyBooking());
        editButton.setOnAction(event -> håndterRedigerBooking());
        cancelBookingButton.setOnAction(event -> håndterAnnullerBooking());

        // Skifter til listevisning hvis brugeren vælger det
        customerBookingsComboBox.setOnAction(event -> {
            String valgtVisning = customerBookingsComboBox.getValue();
            if (valgtVisning == null) return;

            try {
                if (valgtVisning.equals("Listevisning")) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projektopgave1/ListeOversigt.fxml"));
                    Parent root = loader.load();

                    Scene currentScene = customerBookingsComboBox.getScene();
                    Stage stage = (Stage) currentScene.getWindow();

                    Scene newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
                    stage.setScene(newScene);

                    LoggerUtility.logEvent("Skiftet til listevisning");
                }
            } catch (Exception e) {
                LoggerUtility.logError("Fejl ved skift til listevisning: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Denne metode gør egentlig kun, at når man vælger Alle medarbejdere, så er alle valgt.
    private void opdaterMedarbejderCheckboxTilstand() {
        boolean alleValgt = allBookingsCheckBox.isSelected();
        jonCheckBox.setDisable(alleValgt);
        joachimCheckBox.setDisable(alleValgt);
        lasseCheckBox.setDisable(alleValgt);
        gabrielCheckBox.setDisable(alleValgt);
    }


    private void sætStartTilstandOp() {
        // Deaktivere redigerings og annuller knapper indtil en booking er valgt
        editButton.setDisable(true);
        cancelBookingButton.setDisable(true);

        // Vælger alle medarbejdere som standard
        jonCheckBox.setSelected(true);
        joachimCheckBox.setSelected(true);
        lasseCheckBox.setSelected(true);
        gabrielCheckBox.setSelected(true);
        allBookingsCheckBox.setSelected(true);
        opdaterMedarbejderCheckboxTilstand();
    }

     // Opdaterer dato labelen med den aktuelle uge
    private void opdaterDatoLabel() {
        currentDateLabel.setText(useCaseCalendar.getFormattedDateRange());
    }


    // Opretter et grid. StackOverflow er benyttet til dette
    private void opsætKalenderGrid() {
        for (int col = 1; col <= 7; col++) {
            for (int row = 0; row <= 20; row++) {
                Pane cell = new Pane();
                cell.setStyle("-fx-border-color: #e8e8e8; -fx-border-width: 0.5;");
                calendarGrid.add(cell, col, row);

                // Gemmer kolonnen og rækken så vi ved hvilken celle der blev klikket på
                final int dagKolonne = col;
                final int tidRække = row;
                cell.setOnMouseClicked(event -> håndterCelleKlik(dagKolonne, tidRække));
            }
        }
    }

    // Refresher i bund og grund bare kalendaren, om der er ændringer.
    private void genindlæsKalender() {
        try {
            // Fjerner alle eksisterende bookinger fra grid
            fjernBookingsFraGrid();

            // Nulstiller valgte bookings
            selectedAppointment = null;
            useCaseCalendar.clearSelectedAppointment();
            editButton.setDisable(true);
            cancelBookingButton.setDisable(true);

            // Viser de filtrerede bookinger baseret på valgte medarbejdere
            visFilteredeBookinger();
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved genindlæsning af kalender: " + e.getMessage());
        }
    }

    // Fjerner alle bokse
    private void fjernBookingsFraGrid() {
        calendarGrid.getChildren().removeIf(node ->
                node instanceof VBox && node.getStyleClass().contains("appointment-box"));
    }

    // Vores filtrering der viser hvilken medarbejder der er valgt
    private void visFilteredeBookinger() {
        // Tjekker hvilke medarbejdere der er valgt
        boolean visJon = jonCheckBox.isSelected() || allBookingsCheckBox.isSelected();
        boolean visJoachim = joachimCheckBox.isSelected() || allBookingsCheckBox.isSelected();
        boolean visLasse = lasseCheckBox.isSelected() || allBookingsCheckBox.isSelected();
        boolean visGabriel = gabrielCheckBox.isSelected() || allBookingsCheckBox.isSelected();

        // Henter alle bookinger for de valgte medarbejdere
        List<AppointmentData> bookinger = useCaseCalendar.getFilteredAppointments(visJon, visJoachim, visLasse, visGabriel);

        // Opreter og viser hver booking i gridden
        for (AppointmentData data : bookinger) {
            VBox bookingBoks = opretBookingBoks(
                    data.getId(),
                    data.getCustomerName(),
                    data.getTreatment(),
                    data.getEmployee(),
                    data.getStartTime(),
                    data.getEndTime(),
                    data.getDayOfWeek()
            );

            // Finder ud af hvor i grid boksen skal placeres
            int startRække = useCaseCalendar.timeToRow(data.getStartTime());
            int slutRække = useCaseCalendar.timeToRow(data.getEndTime());
            int antalRækker = Math.max(1, slutRække - startRække);

            // Tilføjer boksen til grid
            calendarGrid.add(bookingBoks, data.getDayOfWeek(), startRække, 1, antalRækker);
        }
    }

    // Denne kode er taget fra ChatGPT. Den opretter i bund og grund de bokse der kan ses på kalendervisningen
    private VBox opretBookingBoks(int id, String kundeNavn, String behandling, String medarbejder,
                                  LocalTime startTid, LocalTime slutTid, int ugedag) {
        // Opretter VBox til at vise booking information
        VBox bookingBoks = new VBox(5);
        bookingBoks.getStyleClass().add("appointment-box");
        bookingBoks.setPadding(new Insets(5));

        // Sætter baggrund og kant
        bookingBoks.setBackground(new Background(new BackgroundFill(
                Color.rgb(229, 224, 183, 0.9),
                new CornerRadii(4),
                Insets.EMPTY)));
        bookingBoks.setBorder(new Border(new BorderStroke(
                Color.rgb(180, 124, 97, 0.8),
                BorderStrokeStyle.SOLID,
                new CornerRadii(4),
                new BorderWidths(1))));

        // Opretter labels med information
        Label navnLabel = new Label(kundeNavn);
        navnLabel.setStyle("-fx-font-weight: bold;");

        Label behandlingsLabel = new Label(behandling);

        Label medarbejderLabel = new Label(medarbejder);
        medarbejderLabel.setStyle("-fx-font-style: italic;");

        Label tidsLabel = new Label(
                startTid.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                        slutTid.format(DateTimeFormatter.ofPattern("HH:mm"))
        );

        // Tilføjer labels til boksen
        bookingBoks.getChildren().addAll(navnLabel, behandlingsLabel, medarbejderLabel, tidsLabel);

        // Gemmer booking ID så vi ved hvilken booking det er
        bookingBoks.setUserData(id);

        // Tilføjer klik handler så man kan vælge bookingen
        bookingBoks.setOnMouseClicked(event -> håndterBookingKlik(bookingBoks));

        return bookingBoks;
    }

    // Denne kode kører når vi trykker på en booking
    private void håndterBookingKlik(VBox bookingBoks) {
        try {
            // Hvis der allerede er valgt en booking, fjernes markeringen
            if (selectedAppointment != null) {
                selectedAppointment.setBorder(new Border(new BorderStroke(
                        Color.rgb(180, 124, 97, 0.8),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(4),
                        new BorderWidths(1))));
            }

            // Sætter den nye valgte booking
            selectedAppointment = bookingBoks;
            int bookingId = (int) bookingBoks.getUserData();
            useCaseCalendar.setSelectedAppointmentId(bookingId);

            // Markere den valgte booking med blå kant
            bookingBoks.setBorder(new Border(new BorderStroke(
                    Color.rgb(0, 120, 215),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(4),
                    new BorderWidths(2))));

            // Aktivere redigerings og annuller knapper
            editButton.setDisable(false);
            cancelBookingButton.setDisable(false);
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved valg af booking: " + e.getMessage());
        }
    }

    // Denne metode håndtere når brugeren trykker på en tom celle. Bruges også til at "deselect" en aktiv celle.
    private void håndterCelleKlik(int dagKolonne, int tidRække) {
        if (selectedAppointment != null) {
            selectedAppointment.setBorder(new Border(new BorderStroke(
                    Color.rgb(180, 124, 97, 0.8),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(4),
                    new BorderWidths(1))));

            selectedAppointment = null;
            useCaseCalendar.clearSelectedAppointment();

            editButton.setDisable(true);
            cancelBookingButton.setDisable(true);
        }
    }

    // Denne funktion kører når brugeren vil oprette en ny booking.
    private void håndterNyBooking() {
        try {
            // Indlæser opret booking vinduet
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projektopgave1/OpretBooking.fxml"));
            Parent page = loader.load();

            // Opretter vores popup vindue
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Opret Ny Booking");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(calendarGrid.getScene().getWindow());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Sætter controlleren op
            OpretBookingController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Viser vinduet og venter til brugeren lukker det
            dialogStage.showAndWait();

            // Hvis der blev gemt en booking, opdateres kalenderen
            if (controller.isSaveClicked()) {
                genindlæsKalender();
            }
        } catch (IOException e) {
            LoggerUtility.logError("Fejl ved åbning af booking-dialog: " + e.getMessage());
        }
    }

    // Brugeren trykker på Rediger booking, kører denne metode
    private void håndterRedigerBooking() {
        try {
            // Tjek at der er valgt en booking
            if (selectedAppointment == null) return;

            // Henter den valgte booking
            int bookingId = useCaseCalendar.getSelectedAppointmentId();
            AppointmentData currentData = useCaseCalendar.getAppointmentById(bookingId);
            if (currentData == null) return;

            // Opreter en dialog til at redigere bookingen
            Dialog<ButtonType> dialog = opretRedigerDialog(bookingId, currentData);
            Optional<ButtonType> result = dialog.showAndWait();

            // Hvis brugeren trykker ok, gemmer vi ændringerne
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                behandlRedigerDialogResultat(dialog, bookingId);
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved redigering af booking: " + e.getMessage());
        }
    }

    // Redigere en booking vindue
    private Dialog<ButtonType> opretRedigerDialog(int bookingId, AppointmentData currentData) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Rediger Booking");
        dialog.setHeaderText("Rediger booking #" + bookingId);

        ButtonType saveButtonType = new ButtonType("Gem", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Kunde ComboBox med værdier fra databasen
        ComboBox<String> customerCombo = new ComboBox<>();
        customerCombo.getItems().addAll(useCaseCalendar.getAllCustomerNames());
        customerCombo.setValue(currentData.getCustomerName());

        // Behandling ComboBox med værdier fra databasen
        ComboBox<String> treatmentCombo = new ComboBox<>();
        treatmentCombo.getItems().addAll(useCaseCalendar.getAllTreatmentNames());
        treatmentCombo.setValue(currentData.getTreatment());

        // Medarbejder ComboBox med værdier fra databasen
        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.getItems().addAll(useCaseCalendar.getAllEmployeeNames());
        employeeCombo.setValue(currentData.getEmployee());

        DatePicker datePicker = new DatePicker(currentData.getDate());

        ComboBox<String> startTimeCombo = new ComboBox<>();
        ComboBox<String> endTimeCombo = new ComboBox<>();

        fyldTidsComboBoxes(startTimeCombo, endTimeCombo);

        startTimeCombo.setValue(currentData.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        endTimeCombo.setValue(currentData.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        grid.add(new Label("Kunde:"), 0, 0);
        grid.add(customerCombo, 1, 0);
        grid.add(new Label("Behandling:"), 0, 1);
        grid.add(treatmentCombo, 1, 1);
        grid.add(new Label("Medarbejder:"), 0, 2);
        grid.add(employeeCombo, 1, 2);
        grid.add(new Label("Dato:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Starttid:"), 0, 4);
        grid.add(startTimeCombo, 1, 4);
        grid.add(new Label("Sluttid:"), 0, 5);
        grid.add(endTimeCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(customerCombo::requestFocus);

        return dialog;
    }

    // Denne metode gør de gule bokse i vores kalendervisningen fylde mere, baseret på behandlingens varighed.
    private void fyldTidsComboBoxes(ComboBox<String> startTimeCombo, ComboBox<String> endTimeCombo) {
        for (int time = 8; time <= 18; time++) {
            startTimeCombo.getItems().add(String.format("%02d:00", time));
            endTimeCombo.getItems().add(String.format("%02d:00", time));

            if (time < 18) {
                startTimeCombo.getItems().add(String.format("%02d:30", time));
                endTimeCombo.getItems().add(String.format("%02d:30", time));
            }
        }
    }

    // Denne metode behandler når vi redigere i en booking
    private void behandlRedigerDialogResultat(Dialog<ButtonType> dialog, int bookingId) {
        GridPane grid = (GridPane) dialog.getDialogPane().getContent();

        // Hent alle værdier fra felterne
        ComboBox<String> customerCombo = (ComboBox<String>) getNodeByRowColumnIndex(0, 1, grid);
        ComboBox<String> treatmentCombo = (ComboBox<String>) getNodeByRowColumnIndex(1, 1, grid);
        ComboBox<String> employeeCombo = (ComboBox<String>) getNodeByRowColumnIndex(2, 1, grid);
        DatePicker datePicker = (DatePicker) getNodeByRowColumnIndex(3, 1, grid);
        ComboBox<String> startTimeCombo = (ComboBox<String>) getNodeByRowColumnIndex(4, 1, grid);
        ComboBox<String> endTimeCombo = (ComboBox<String>) getNodeByRowColumnIndex(5, 1, grid);

        // Konvertere tider fra strenge til LocalTime
        String startTimeStr = startTimeCombo.getValue();
        String endTimeStr = endTimeCombo.getValue();
        LocalTime startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDate selectedDate = datePicker.getValue();

        // Opdatere bookingen med de nye værdier
        useCaseCalendar.updateAppointment(
                bookingId,
                customerCombo.getValue(),
                treatmentCombo.getValue(),
                employeeCombo.getValue(),
                startTime,
                endTime,
                selectedDate
        );

        // Genindlæser kalenderen så ændringerne vises
        genindlæsKalender();

        // Vis en bekræftelse til brugeren
        visInfoAlert("Booking Opdateret", null,
                "Booking #" + bookingId + " er blevet opdateret.");
    }

     // Finder et element i GridPane baseret på række og kolonne
    private javafx.scene.Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                return node;
            }
        }
        return null;
    }

    // Denne metode håndtere når brugeren vil annullere en aktiv booking
    private void håndterAnnullerBooking() {
        try {
            // Tjek at der er valgt en booking
            if (selectedAppointment == null)
                return;

            // Henter booking ID
            int bookingId = useCaseCalendar.getSelectedAppointmentId();

            // Vis en bekræftelse dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Bekræft Annullering");
            confirmAlert.setHeaderText("Er du sikker på, at du vil annullere denne booking?");
            confirmAlert.setContentText("Booking #" + bookingId);

            Optional<ButtonType> result = confirmAlert.showAndWait();

            // Hvis brugeren bekræfter, annulleres bookingen
            if (result.isPresent() && result.get() == ButtonType.OK) {
                useCaseCalendar.deleteAppointment(bookingId);
                genindlæsKalender(); // Genindlæser kalenderen så brugeren ikke stadig ser det gamle
                visInfoAlert("Booking Annulleret", null, "Booking er blevet annulleret.");
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved annullering af booking: " + e.getMessage());
        }
    }

    // Viser en alert
    private void visInfoAlert(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}