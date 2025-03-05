package com.example.projektopgave1.Controller;

import com.example.projektopgave1.Model.DatabaseHandlers.KundeDatabaseHandler;
import com.example.projektopgave1.Model.UseCases.UseCaseOpretBooking;
import Utils.LoggerUtility;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class OpretBookingController {
    @FXML
    private ComboBox<String> customerComboBox;
    @FXML
    private ComboBox<String> employeeComboBox;
    @FXML
    private ComboBox<String> treatmentComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> startTimeComboBox;
    @FXML
    private Button saveBookingButton;
    @FXML
    private Button cancelButton;

    // Fields for the second tab (Opret Ny Kunde)
    @FXML
    private TextField nameTextField;
    @FXML
    private TextField phoneTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField addressTextField;
    @FXML
    private Button saveCustomerButton;

    private UseCaseOpretBooking useCaseOpretBooking;
    private Stage dialogStage;
    private boolean saveClicked = false;

    @FXML
    public void initialize() {
        useCaseOpretBooking = new UseCaseOpretBooking();
        populateEmployeeComboBox();
        populateTreatmentComboBox();
        populateTimeComboBox();

        // Set default date to today
        datePicker.setValue(LocalDate.now());

        Platform.runLater(() -> customerComboBox.requestFocus());
    }

    private void populateEmployeeComboBox() {
        employeeComboBox.getItems().addAll(useCaseOpretBooking.getAllEmployeeNames());
    }

    private void populateTreatmentComboBox() {
        treatmentComboBox.getItems().addAll(useCaseOpretBooking.getAllTreatmentNames());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    private void populateTimeComboBox() {
        for (int hour = 8; hour <= 18; hour++) {
            startTimeComboBox.getItems().add(String.format("%02d:00", hour));
            if (hour < 18) { // Don't add 18:30 as it would go beyond business hours
                startTimeComboBox.getItems().add(String.format("%02d:30", hour));
            }
        }
    }

    @FXML
    private void handleSaveBooking() {
        String customer = customerComboBox.getValue();
        String employee = employeeComboBox.getValue();
        String treatment = treatmentComboBox.getValue();
        LocalDate date = datePicker.getValue();
        String startTimeStr = startTimeComboBox.getValue();

        if (customer == null || employee == null || treatment == null || date == null || startTimeStr == null) {
            showAlert("Fejl", "Alle felter skal udfyldes.");
            return;
        }

        LocalTime startTime;
        try {
            startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            showAlert("Fejl", "Ugyldigt tidformat.");
            return;
        }

        // Calculate end time based on treatment duration
        int duration = useCaseOpretBooking.getTreatmentDuration(treatment);
        LocalTime endTime = startTime.plusMinutes(duration);

        // Check if time slot is available
        if (!useCaseOpretBooking.isTimeSlotAvailable(employee, date, startTime, endTime)) {
            showAlert("Fejl", "Tidsslot er ikke tilgængeligt for " + employee);
            return;
        }

        // Try to create the booking
        boolean success = useCaseOpretBooking.createBooking(customer, treatment, employee, date, startTime, endTime);

        if (success) {
            saveClicked = true;
            showAlert("Succes", "Booking oprettet.");
            if (dialogStage != null) {
                dialogStage.close();
            }
        } else {
            showAlert("Fejl", "Booking kunne ikke oprettes.");
        }
    }

    @FXML
    private void handleSaveCustomer() {
        // Validate input
        if (nameTextField == null || nameTextField.getText() == null || nameTextField.getText().trim().isEmpty()) {
            showAlert("Fejl", "Kundenavn skal udfyldes.");
            return;
        }

        try {
            // Create a new customer and add to database
            com.example.projektopgave1.Model.Entiteter.Kunde newCustomer = new com.example.projektopgave1.Model.Entiteter.Kunde(
                    0,
                    nameTextField.getText().trim(),
                    phoneTextField.getText() != null ? phoneTextField.getText().trim() : "",
                    emailTextField.getText() != null ? emailTextField.getText().trim() : "",
                    addressTextField.getText() != null ? addressTextField.getText().trim() : ""
            );

            // Use the KundeDatabaseHandler directly
            KundeDatabaseHandler kundeDatabaseHandler = new KundeDatabaseHandler();
            com.example.projektopgave1.Model.Entiteter.Kunde savedCustomer = kundeDatabaseHandler.create(newCustomer);

            if (savedCustomer != null && savedCustomer.getKundeID() > 0) {
                // Add to the customer ComboBox
                customerComboBox.getItems().add(savedCustomer.getNavn());
                customerComboBox.setValue(savedCustomer.getNavn());

                // Switch back to the first tab
                TabPane tabPane = (TabPane) nameTextField.getScene().lookup("#tabPane");
                if (tabPane != null) {
                    tabPane.getSelectionModel().select(0);
                }

                showAlert("Succes", "Kunde oprettet og tilføjet.");
            } else {
                showAlert("Fejl", "Kunde kunne ikke oprettes.");
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved oprettelse af kunde: " + e.getMessage());
            showAlert("Fejl", "Der opstod en fejl: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        saveClicked = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}