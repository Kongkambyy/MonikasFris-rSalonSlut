package com.example.projektopgave1.Controller;

import com.example.projektopgave1.CustomExceptions.BookingNotPossibleException;
import com.example.projektopgave1.CustomExceptions.DatabaseConnectionException;
import com.example.projektopgave1.CustomExceptions.HairdresserBusyException;
import com.example.projektopgave1.CustomExceptions.TimeSlotUnavailableException;
import com.example.projektopgave1.Model.Entiteter.Aftale;
import com.example.projektopgave1.Model.UseCases.UseCase;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    private UseCase useCase;
    private LocalDate currentDate;

    @FXML
    public void initialize() {
        useCase = new UseCase();
        currentDate = LocalDate.now();
        updateCurrentDateLabel();
        customerBookingsComboBox.setItems(FXCollections.observableArrayList("Alle Kunder", "Alle Bookinger", "Aktive Bookinger"));
        todayButton.setOnAction(e -> {
            currentDate = LocalDate.now();
            updateCurrentDateLabel();
            loadCalendar();
        });
        prevButton.setOnAction(e -> {
            currentDate = currentDate.minusWeeks(1);
            updateCurrentDateLabel();
            loadCalendar();
        });
        nextButton.setOnAction(e -> {
            currentDate = currentDate.plusWeeks(1);
            updateCurrentDateLabel();
            loadCalendar();
        });
        newBookingButton.setOnAction(e -> handleNewBooking());
        editButton.setOnAction(e -> handleEditBooking());
        cancelBookingButton.setOnAction(e -> handleCancelBooking());
        loadCalendar();
    }

    private void updateCurrentDateLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        currentDateLabel.setText(currentDate.format(formatter));
    }

    private void loadCalendar() {
        calendarGrid.getChildren().removeIf(node -> {
            Integer row = GridPane.getRowIndex(node);
            Integer col = GridPane.getColumnIndex(node);
            return (row != null && row >= 1 && col != null && col >= 1);
        });
        LocalDate startOfWeek = currentDate.with(DayOfWeek.SUNDAY);
        for (int day = 0; day < 7; day++) {
            LocalDate date = startOfWeek.plusDays(day);
            try {
                List<Aftale> appointments = useCase.getAppointmentsByDate(date);
                for (Aftale app : appointments) {
                    LocalTime startTime = app.getStarttidspunkt().toLocalTime();
                    int rowIndex = ((startTime.getHour() - 8) * 2) + (startTime.getMinute() >= 30 ? 2 : 1);
                    Label label = new Label("Booking " + app.getAftaleID());
                    calendarGrid.add(label, day + 1, rowIndex);
                }
            } catch (DatabaseConnectionException ex) {
                showAlert("Fejl", "Databasefejl: " + ex.getMessage(), AlertType.ERROR);
            }
        }
    }

    private void handleNewBooking() {
        try {
            int kundeId = 1;
            int medarbejderId = 1;
            int behandlingId = 1;
            LocalDateTime startDateTime = LocalDateTime.of(currentDate.plusDays(1), LocalTime.of(10, 0));
            Aftale newApp = useCase.createAppointment(kundeId, medarbejderId, behandlingId, startDateTime);
            showAlert("Ny Booking", "Booking oprettet med id: " + newApp.getAftaleID(), AlertType.INFORMATION);
            loadCalendar();
        } catch (DatabaseConnectionException | HairdresserBusyException | TimeSlotUnavailableException | BookingNotPossibleException ex) {
            showAlert("Fejl", ex.getMessage(), AlertType.ERROR);
        }
    }

    private void handleEditBooking() {
        try {
            int appointmentId = 1;
            LocalDateTime newStart = LocalDateTime.of(currentDate.plusDays(2), LocalTime.of(11, 0));
            Aftale updatedApp = useCase.moveAppointment(appointmentId, newStart);
            showAlert("Booking Opdateret", "Ny starttid: " + updatedApp.getStarttidspunkt(), AlertType.INFORMATION);
            loadCalendar();
        } catch (DatabaseConnectionException | HairdresserBusyException | TimeSlotUnavailableException | BookingNotPossibleException ex) {
            showAlert("Fejl", ex.getMessage(), AlertType.ERROR);
        }
    }

    private void handleCancelBooking() {
        try {
            int appointmentId = 1;
            boolean success = useCase.cancelAppointment(appointmentId);
            if (success) {
                showAlert("Booking Annulleret", "Booking er blevet annulleret.", AlertType.INFORMATION);
            } else {
                showAlert("Fejl", "Booking ikke fundet.", AlertType.ERROR);
            }
            loadCalendar();
        } catch (DatabaseConnectionException ex) {
            showAlert("Fejl", ex.getMessage(), AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
