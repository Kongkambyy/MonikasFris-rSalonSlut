package com.example.projektopgave1.Controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.application.Platform;
import javafx.scene.Node;

import java.time.DayOfWeek;
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

    private LocalDate currentDate = LocalDate.now();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private VBox selectedAppointment = null;
    private int selectedAppointmentId = -1;

    private Set<Integer> deletedAppointmentIds = new HashSet<>();
    private Map<Integer, AppointmentData> appointmentsMap = new HashMap<>();

    private static class AppointmentData {
        int id;
        String customerName;
        String treatment;
        String employee;
        LocalTime startTime;
        LocalTime endTime;
        LocalDate date;

        AppointmentData(int id, String customerName, String treatment, String employee,
                        LocalTime startTime, LocalTime endTime, LocalDate date) {
            this.id = id;
            this.customerName = customerName;
            this.treatment = treatment;
            this.employee = employee;
            this.startTime = startTime;
            this.endTime = endTime;
            this.date = date;
        }

        int getDayOfWeek() {
            return date.getDayOfWeek().getValue();
        }
    }

    @FXML
    public void initialize() {
        opretEksempelBookinger();

        opdaterDatoLabel();

        todayButton.setOnAction(event -> {
            currentDate = LocalDate.now();
            opdaterDatoLabel();
            genindlaesKalender();
        });

        prevButton.setOnAction(event -> {
            currentDate = currentDate.minusWeeks(1);
            opdaterDatoLabel();
            genindlaesKalender();
        });

        nextButton.setOnAction(event -> {
            currentDate = currentDate.plusWeeks(1);
            opdaterDatoLabel();
            genindlaesKalender();
        });

        allBookingsCheckBox.setOnAction(event -> {
            boolean alleValgt = allBookingsCheckBox.isSelected();
            jonCheckBox.setDisable(alleValgt);
            joachimCheckBox.setDisable(alleValgt);
            lasseCheckBox.setDisable(alleValgt);
            gabrielCheckBox.setDisable(alleValgt);
            genindlaesKalender();
        });

        jonCheckBox.setOnAction(event -> genindlaesKalender());
        joachimCheckBox.setOnAction(event -> genindlaesKalender());
        lasseCheckBox.setOnAction(event -> genindlaesKalender());
        gabrielCheckBox.setOnAction(event -> genindlaesKalender());

        newBookingButton.setOnAction(event -> haandterNyBooking());
        editButton.setOnAction(event -> haandterRedigerBooking());
        cancelBookingButton.setOnAction(event -> haandterAnnullerBooking());

        editButton.setDisable(true);
        cancelBookingButton.setDisable(true);

        jonCheckBox.setSelected(true);
        joachimCheckBox.setSelected(true);
        lasseCheckBox.setSelected(true);
        gabrielCheckBox.setSelected(true);
        jonCheckBox.setDisable(true);
        joachimCheckBox.setDisable(true);
        lasseCheckBox.setDisable(true);
        gabrielCheckBox.setDisable(true);

        opsaetKalenderGrid();

        genindlaesKalender();
    }

    private void opretEksempelBookinger() {
        LocalDate idag = LocalDate.now();

        appointmentsMap.put(1, new AppointmentData(
                1, "Anders Hansen", "Klip og vask", "Jon",
                LocalTime.of(10, 0), LocalTime.of(10, 45),
                idag.with(DayOfWeek.TUESDAY)
        ));

        appointmentsMap.put(2, new AppointmentData(
                2, "Mette Jensen", "Farve og klip", "Joachim",
                LocalTime.of(13, 30), LocalTime.of(15, 0),
                idag.with(DayOfWeek.THURSDAY)
        ));

        appointmentsMap.put(3, new AppointmentData(
                3, "Søren Pedersen", "Barbering", "Lasse",
                LocalTime.of(9, 30), LocalTime.of(10, 0),
                idag.with(DayOfWeek.FRIDAY)
        ));

        appointmentsMap.put(4, new AppointmentData(
                4, "Lise Nielsen", "Hårfarvning", "Gabriel",
                LocalTime.of(14, 0), LocalTime.of(15, 30),
                idag.with(DayOfWeek.MONDAY)
        ));

        appointmentsMap.put(5, new AppointmentData(
                5, "Peter Madsen", "Klip og skæg", "Jon",
                LocalTime.of(11, 0), LocalTime.of(12, 0),
                idag.plusWeeks(1).with(DayOfWeek.WEDNESDAY)
        ));
    }

    private void opsaetKalenderGrid() {
        for (int col = 1; col <= 7; col++) {
            for (int row = 0; row <= 20; row++) {
                Pane celle = new Pane();
                celle.setStyle("-fx-border-color: #e8e8e8; -fx-border-width: 0.5;");
                calendarGrid.add(celle, col, row);

                final int dagKolonne = col;
                final int tidRaekke = row;
                celle.setOnMouseClicked(event -> haandterCelleKlik(dagKolonne, tidRaekke));
            }
        }
    }

    private void opdaterDatoLabel() {
        LocalDate ugeStart = currentDate.with(DayOfWeek.MONDAY);
        LocalDate ugeSlut = ugeStart.plusDays(6);

        String datoTekst = ugeStart.format(dateFormatter) + " - " + ugeSlut.format(dateFormatter);
        currentDateLabel.setText(datoTekst);
    }

    private void genindlaesKalender() {
        fjernBookingerFraGrid();

        selectedAppointment = null;
        selectedAppointmentId = -1;
        editButton.setDisable(true);
        cancelBookingButton.setDisable(true);

        visFilteredeBookinger();
    }

    private void fjernBookingerFraGrid() {
        calendarGrid.getChildren().removeIf(node ->
                node instanceof VBox && node.getStyleClass().contains("appointment-box"));
    }

    private void visFilteredeBookinger() {
        boolean visJon = jonCheckBox.isSelected() || allBookingsCheckBox.isSelected();
        boolean visJoachim = joachimCheckBox.isSelected() || allBookingsCheckBox.isSelected();
        boolean visLasse = lasseCheckBox.isSelected() || allBookingsCheckBox.isSelected();
        boolean visGabriel = gabrielCheckBox.isSelected() || allBookingsCheckBox.isSelected();

        LocalDate ugeStart = currentDate.with(DayOfWeek.MONDAY);
        LocalDate ugeSlut = ugeStart.plusDays(6);

        for (Map.Entry<Integer, AppointmentData> entry : appointmentsMap.entrySet()) {
            int bookingId = entry.getKey();
            AppointmentData data = entry.getValue();

            if (deletedAppointmentIds.contains(bookingId)) {
                continue;
            }

            if (data.date.isBefore(ugeStart) || data.date.isAfter(ugeSlut)) {
                continue;
            }

            boolean skalVises = false;

            switch (data.employee) {
                case "Jon":
                    skalVises = visJon;
                    break;
                case "Joachim":
                    skalVises = visJoachim;
                    break;
                case "Lasse":
                    skalVises = visLasse;
                    break;
                case "Gabriel":
                    skalVises = visGabriel;
                    break;
            }

            if (skalVises) {
                VBox bookingBoks = opretBookingBoks(
                        data.id,
                        data.customerName,
                        data.treatment,
                        data.employee,
                        data.startTime,
                        data.endTime,
                        data.getDayOfWeek()
                );

                int startRaekke = tidTilRaekke(data.startTime);
                int slutRaekke = tidTilRaekke(data.endTime);
                int raekkeSpan = Math.max(1, slutRaekke - startRaekke);

                calendarGrid.add(bookingBoks, data.getDayOfWeek(), startRaekke, 1, raekkeSpan);
            }
        }
    }

    private VBox opretBookingBoks(int id, String kundensNavn, String behandling, String medarbejder,
                                  LocalTime startTid, LocalTime slutTid, int ugedag) {
        VBox bookingBoks = new VBox(5);
        bookingBoks.getStyleClass().add("appointment-box");
        bookingBoks.setPadding(new Insets(5));

        bookingBoks.setBackground(new Background(new BackgroundFill(
                Color.rgb(229, 224, 183, 0.9),
                new CornerRadii(4),
                Insets.EMPTY)));
        bookingBoks.setBorder(new Border(new BorderStroke(
                Color.rgb(180, 124, 97, 0.8),
                BorderStrokeStyle.SOLID,
                new CornerRadii(4),
                new BorderWidths(1))));

        Label navnLabel = new Label(kundensNavn);
        navnLabel.setStyle("-fx-font-weight: bold;");

        Label behandlingLabel = new Label(behandling);

        Label medarbejderLabel = new Label(medarbejder);
        medarbejderLabel.setStyle("-fx-font-style: italic;");

        Label tidLabel = new Label(
                startTid.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                        slutTid.format(DateTimeFormatter.ofPattern("HH:mm"))
        );

        bookingBoks.getChildren().addAll(navnLabel, behandlingLabel, medarbejderLabel, tidLabel);

        bookingBoks.setUserData(id);

        bookingBoks.setOnMouseClicked(event -> haandterBookingKlik(bookingBoks));

        return bookingBoks;
    }

    private void haandterBookingKlik(VBox bookingBoks) {
        if (selectedAppointment != null) {
            selectedAppointment.setBorder(new Border(new BorderStroke(
                    Color.rgb(180, 124, 97, 0.8),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(4),
                    new BorderWidths(1))));
        }

        selectedAppointment = bookingBoks;
        selectedAppointmentId = (int) bookingBoks.getUserData();

        bookingBoks.setBorder(new Border(new BorderStroke(
                Color.rgb(0, 120, 215),
                BorderStrokeStyle.SOLID,
                new CornerRadii(4),
                new BorderWidths(2))));

        editButton.setDisable(false);
        cancelBookingButton.setDisable(false);
    }

    private void haandterCelleKlik(int dagKolonne, int tidRaekke) {
        if (selectedAppointment != null) {
            selectedAppointment.setBorder(new Border(new BorderStroke(
                    Color.rgb(180, 124, 97, 0.8),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(4),
                    new BorderWidths(1))));

            selectedAppointment = null;
            selectedAppointmentId = -1;

            editButton.setDisable(true);
            cancelBookingButton.setDisable(true);
        }
    }

    private void haandterNyBooking() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ny Booking");
        alert.setHeaderText("Opret ny booking");
        alert.setContentText("Her ville du normalt se en dialog til at oprette en ny booking.");
        alert.showAndWait();
    }

    private void haandterRedigerBooking() {
        if (selectedAppointment == null) return;

        AppointmentData nuværendeData = appointmentsMap.get(selectedAppointmentId);
        if (nuværendeData == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Rediger Booking");
        dialog.setHeaderText("Rediger booking #" + selectedAppointmentId);

        ButtonType saveButtonType = new ButtonType("Gem", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField kundeField = new TextField(nuværendeData.customerName);
        TextField behandlingField = new TextField(nuværendeData.treatment);

        ComboBox<String> medarbejderCombo = new ComboBox<>();
        medarbejderCombo.getItems().addAll("Jon", "Joachim", "Lasse", "Gabriel");
        medarbejderCombo.setValue(nuværendeData.employee);

        DatePicker datePicker = new DatePicker(nuværendeData.date);

        ComboBox<String> startTidCombo = new ComboBox<>();
        ComboBox<String> slutTidCombo = new ComboBox<>();

        for (int time = 8; time <= 18; time++) {
            startTidCombo.getItems().add(String.format("%02d:00", time));
            slutTidCombo.getItems().add(String.format("%02d:00", time));

            if (time < 18) {
                startTidCombo.getItems().add(String.format("%02d:30", time));
                slutTidCombo.getItems().add(String.format("%02d:30", time));
            }
        }

        startTidCombo.setValue(nuværendeData.startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        slutTidCombo.setValue(nuværendeData.endTime.format(DateTimeFormatter.ofPattern("HH:mm")));

        grid.add(new Label("Kunde:"), 0, 0);
        grid.add(kundeField, 1, 0);
        grid.add(new Label("Behandling:"), 0, 1);
        grid.add(behandlingField, 1, 1);
        grid.add(new Label("Medarbejder:"), 0, 2);
        grid.add(medarbejderCombo, 1, 2);
        grid.add(new Label("Dato:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Starttid:"), 0, 4);
        grid.add(startTidCombo, 1, 4);
        grid.add(new Label("Sluttid:"), 0, 5);
        grid.add(slutTidCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(kundeField::requestFocus);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == saveButtonType) {
            String startTidStr = startTidCombo.getValue();
            String slutTidStr = slutTidCombo.getValue();
            LocalTime startTid = LocalTime.parse(startTidStr, DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime slutTid = LocalTime.parse(slutTidStr, DateTimeFormatter.ofPattern("HH:mm"));

            LocalDate valgtDato = datePicker.getValue();

            AppointmentData opdateretData = new AppointmentData(
                    selectedAppointmentId,
                    kundeField.getText(),
                    behandlingField.getText(),
                    medarbejderCombo.getValue(),
                    startTid,
                    slutTid,
                    valgtDato
            );

            appointmentsMap.put(selectedAppointmentId, opdateretData);

            genindlaesKalender();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Booking Opdateret");
            alert.setHeaderText(null);
            alert.setContentText("Booking #" + selectedAppointmentId + " er blevet opdateret.");
            alert.showAndWait();
        }
    }

    private void haandterAnnullerBooking() {
        if (selectedAppointment == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Bekræft Annullering");
        confirmAlert.setHeaderText("Er du sikker på, at du vil annullere denne booking?");
        confirmAlert.setContentText("Booking #" + selectedAppointmentId);

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            deletedAppointmentIds.add(selectedAppointmentId);

            genindlaesKalender();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Booking Annulleret");
            alert.setHeaderText(null);
            alert.setContentText("Booking er blevet annulleret.");
            alert.showAndWait();
        }
    }

    private int tidTilRaekke(LocalTime tid) {

        int time = tid.getHour();
        int minutter = tid.getMinute();

        return (time - 8) * 2 + (minutter >= 30 ? 1 : 0);
    }
}