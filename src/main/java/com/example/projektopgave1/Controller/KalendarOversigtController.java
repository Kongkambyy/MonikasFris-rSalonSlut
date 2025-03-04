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
            useCaseCalendar = new UseCaseCalendar();

            updateDateLabel();

            setupEventHandlers();

            setupInitialUIState();

            setupCalendarGrid();

            reloadCalendar();

            LoggerUtility.logEvent("Kalenderoversigtvisning initialiseret");
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved initialisering af kalenderoversigt: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        todayButton.setOnAction(event -> {
            useCaseCalendar.moveToToday();
            updateDateLabel();
            reloadCalendar();
        });

        prevButton.setOnAction(event -> {
            useCaseCalendar.moveToPreviousWeek();
            updateDateLabel();
            reloadCalendar();
        });

        nextButton.setOnAction(event -> {
            useCaseCalendar.moveToNextWeek();
            updateDateLabel();
            reloadCalendar();
        });

        allBookingsCheckBox.setOnAction(event -> {
            updateEmployeeCheckboxState();
            reloadCalendar();
        });

        jonCheckBox.setOnAction(event -> reloadCalendar());
        joachimCheckBox.setOnAction(event -> reloadCalendar());
        lasseCheckBox.setOnAction(event -> reloadCalendar());
        gabrielCheckBox.setOnAction(event -> reloadCalendar());

        newBookingButton.setOnAction(event -> handleNewBooking());
        editButton.setOnAction(event -> handleEditBooking());
        cancelBookingButton.setOnAction(event -> handleCancelBooking());

        customerBookingsComboBox.setOnAction(event -> {
            String selectedView = customerBookingsComboBox.getValue();
            if (selectedView == null) return;

            try {
                if (selectedView.equals("Listevisning")) {
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

    private void updateEmployeeCheckboxState() {
        boolean allSelected = allBookingsCheckBox.isSelected();
        jonCheckBox.setDisable(allSelected);
        joachimCheckBox.setDisable(allSelected);
        lasseCheckBox.setDisable(allSelected);
        gabrielCheckBox.setDisable(allSelected);
    }

    private void setupInitialUIState() {
        editButton.setDisable(true);
        cancelBookingButton.setDisable(true);

        jonCheckBox.setSelected(true);
        joachimCheckBox.setSelected(true);
        lasseCheckBox.setSelected(true);
        gabrielCheckBox.setSelected(true);
        allBookingsCheckBox.setSelected(true);
        updateEmployeeCheckboxState();
    }

    private void updateDateLabel() {
        currentDateLabel.setText(useCaseCalendar.getFormattedDateRange());
    }

    private void setupCalendarGrid() {
        for (int col = 1; col <= 7; col++) {
            for (int row = 0; row <= 20; row++) {
                Pane cell = new Pane();
                cell.setStyle("-fx-border-color: #e8e8e8; -fx-border-width: 0.5;");
                calendarGrid.add(cell, col, row);

                final int dayColumn = col;
                final int timeRow = row;
                cell.setOnMouseClicked(event -> handleCellClick(dayColumn, timeRow));
            }
        }
    }

    private void reloadCalendar() {
        try {
            removeBookingsFromGrid();

            selectedAppointment = null;
            useCaseCalendar.clearSelectedAppointment();
            editButton.setDisable(true);
            cancelBookingButton.setDisable(true);

            displayFilteredBookings();
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved genindlæsning af kalender: " + e.getMessage());
        }
    }

    private void removeBookingsFromGrid() {
        calendarGrid.getChildren().removeIf(node ->
                node instanceof VBox && node.getStyleClass().contains("appointment-box"));
    }

    private void displayFilteredBookings() {
        boolean showJon = jonCheckBox.isSelected() || allBookingsCheckBox.isSelected();
        boolean showJoachim = joachimCheckBox.isSelected() || allBookingsCheckBox.isSelected();
        boolean showLasse = lasseCheckBox.isSelected() || allBookingsCheckBox.isSelected();
        boolean showGabriel = gabrielCheckBox.isSelected() || allBookingsCheckBox.isSelected();

        List<AppointmentData> appointments = useCaseCalendar.getFilteredAppointments(showJon, showJoachim, showLasse, showGabriel);

        for (AppointmentData data : appointments) {
            VBox bookingBox = createBookingBox(
                    data.getId(),
                    data.getCustomerName(),
                    data.getTreatment(),
                    data.getEmployee(),
                    data.getStartTime(),
                    data.getEndTime(),
                    data.getDayOfWeek()
            );

            int startRow = useCaseCalendar.timeToRow(data.getStartTime());
            int endRow = useCaseCalendar.timeToRow(data.getEndTime());
            int rowSpan = Math.max(1, endRow - startRow);

            calendarGrid.add(bookingBox, data.getDayOfWeek(), startRow, 1, rowSpan);
        }
    }

    private VBox createBookingBox(int id, String customerName, String treatment, String employee,
                                  LocalTime startTime, LocalTime endTime, int dayOfWeek) {
        VBox bookingBox = new VBox(5);
        bookingBox.getStyleClass().add("appointment-box");
        bookingBox.setPadding(new Insets(5));

        bookingBox.setBackground(new Background(new BackgroundFill(
                Color.rgb(229, 224, 183, 0.9),
                new CornerRadii(4),
                Insets.EMPTY)));
        bookingBox.setBorder(new Border(new BorderStroke(
                Color.rgb(180, 124, 97, 0.8),
                BorderStrokeStyle.SOLID,
                new CornerRadii(4),
                new BorderWidths(1))));

        Label nameLabel = new Label(customerName);
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label treatmentLabel = new Label(treatment);

        Label employeeLabel = new Label(employee);
        employeeLabel.setStyle("-fx-font-style: italic;");

        Label timeLabel = new Label(
                startTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                        endTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        );

        bookingBox.getChildren().addAll(nameLabel, treatmentLabel, employeeLabel, timeLabel);

        bookingBox.setUserData(id);

        bookingBox.setOnMouseClicked(event -> handleBookingClick(bookingBox));

        return bookingBox;
    }

    private void handleBookingClick(VBox bookingBox) {
        try {
            if (selectedAppointment != null) {
                selectedAppointment.setBorder(new Border(new BorderStroke(
                        Color.rgb(180, 124, 97, 0.8),
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(4),
                        new BorderWidths(1))));
            }

            selectedAppointment = bookingBox;
            int appointmentId = (int) bookingBox.getUserData();
            useCaseCalendar.setSelectedAppointmentId(appointmentId);

            bookingBox.setBorder(new Border(new BorderStroke(
                    Color.rgb(0, 120, 215),
                    BorderStrokeStyle.SOLID,
                    new CornerRadii(4),
                    new BorderWidths(2))));

            editButton.setDisable(false);
            cancelBookingButton.setDisable(false);
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved valg af booking: " + e.getMessage());
        }
    }

    private void handleCellClick(int dayColumn, int timeRow) {
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

    private void handleNewBooking() {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ny Booking");
            alert.setHeaderText("Opret ny booking");
            alert.setContentText("Her ville du normalt se en dialog til at oprette en ny booking.");
            alert.showAndWait();
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved håndtering af ny booking: " + e.getMessage());
        }
    }

    private void handleEditBooking() {
        try {
            if (selectedAppointment == null) return;

            int appointmentId = useCaseCalendar.getSelectedAppointmentId();
            AppointmentData currentData = useCaseCalendar.getAppointmentById(appointmentId);
            if (currentData == null) return;

            Dialog<ButtonType> dialog = createEditDialog(appointmentId, currentData);
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                processEditDialogResult(dialog, appointmentId);
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved redigering af booking: " + e.getMessage());
        }
    }

    private Dialog<ButtonType> createEditDialog(int appointmentId, AppointmentData currentData) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Rediger Booking");
        dialog.setHeaderText("Rediger booking #" + appointmentId);

        ButtonType saveButtonType = new ButtonType("Gem", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField customerField = new TextField(currentData.getCustomerName());
        TextField treatmentField = new TextField(currentData.getTreatment());

        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.getItems().addAll("Jon", "Joachim", "Lasse", "Gabriel");
        employeeCombo.setValue(currentData.getEmployee());

        DatePicker datePicker = new DatePicker(currentData.getDate());

        ComboBox<String> startTimeCombo = new ComboBox<>();
        ComboBox<String> endTimeCombo = new ComboBox<>();

        populateTimeComboBoxes(startTimeCombo, endTimeCombo);

        startTimeCombo.setValue(currentData.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        endTimeCombo.setValue(currentData.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));

        addControlsToGrid(grid, customerField, treatmentField, employeeCombo, datePicker, startTimeCombo, endTimeCombo);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(customerField::requestFocus);

        return dialog;
    }

    private void populateTimeComboBoxes(ComboBox<String> startTimeCombo, ComboBox<String> endTimeCombo) {
        for (int hour = 8; hour <= 18; hour++) {
            startTimeCombo.getItems().add(String.format("%02d:00", hour));
            endTimeCombo.getItems().add(String.format("%02d:00", hour));

            if (hour < 18) {
                startTimeCombo.getItems().add(String.format("%02d:30", hour));
                endTimeCombo.getItems().add(String.format("%02d:30", hour));
            }
        }
    }

    private void addControlsToGrid(GridPane grid, TextField customerField, TextField treatmentField,
                                   ComboBox<String> employeeCombo, DatePicker datePicker,
                                   ComboBox<String> startTimeCombo, ComboBox<String> endTimeCombo) {
        grid.add(new Label("Kunde:"), 0, 0);
        grid.add(customerField, 1, 0);
        grid.add(new Label("Behandling:"), 0, 1);
        grid.add(treatmentField, 1, 1);
        grid.add(new Label("Medarbejder:"), 0, 2);
        grid.add(employeeCombo, 1, 2);
        grid.add(new Label("Dato:"), 0, 3);
        grid.add(datePicker, 1, 3);
        grid.add(new Label("Starttid:"), 0, 4);
        grid.add(startTimeCombo, 1, 4);
        grid.add(new Label("Sluttid:"), 0, 5);
        grid.add(endTimeCombo, 1, 5);
    }

    private void processEditDialogResult(Dialog<ButtonType> dialog, int appointmentId) {
        GridPane grid = (GridPane) dialog.getDialogPane().getContent();

        TextField customerField = (TextField) getNodeByRowColumnIndex(0, 1, grid);
        TextField treatmentField = (TextField) getNodeByRowColumnIndex(1, 1, grid);
        ComboBox<String> employeeCombo = (ComboBox<String>) getNodeByRowColumnIndex(2, 1, grid);
        DatePicker datePicker = (DatePicker) getNodeByRowColumnIndex(3, 1, grid);
        ComboBox<String> startTimeCombo = (ComboBox<String>) getNodeByRowColumnIndex(4, 1, grid);
        ComboBox<String> endTimeCombo = (ComboBox<String>) getNodeByRowColumnIndex(5, 1, grid);

        String startTimeStr = startTimeCombo.getValue();
        String endTimeStr = endTimeCombo.getValue();
        LocalTime startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        LocalDate selectedDate = datePicker.getValue();

        useCaseCalendar.updateAppointment(
                appointmentId,
                customerField.getText(),
                treatmentField.getText(),
                employeeCombo.getValue(),
                startTime,
                endTime,
                selectedDate
        );

        reloadCalendar();

        showInfoAlert("Booking Opdateret", null,
                "Booking #" + appointmentId + " er blevet opdateret.");
    }

    private javafx.scene.Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                return node;
            }
        }
        return null;
    }

    private void handleCancelBooking() {
        try {
            if (selectedAppointment == null) return;

            int appointmentId = useCaseCalendar.getSelectedAppointmentId();

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Bekræft Annullering");
            confirmAlert.setHeaderText("Er du sikker på, at du vil annullere denne booking?");
            confirmAlert.setContentText("Booking #" + appointmentId);

            Optional<ButtonType> result = confirmAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                useCaseCalendar.deleteAppointment(appointmentId);
                reloadCalendar();
                showInfoAlert("Booking Annulleret", null, "Booking er blevet annulleret.");
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved annullering af booking: " + e.getMessage());
        }
    }

    private void showInfoAlert(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}