package com.example.projektopgave1.Controller;

import Utils.LoggerUtility;
import com.example.projektopgave1.Model.UseCases.UseCaseCalendar;
import com.example.projektopgave1.Model.UseCases.UseCaseListeOversigt;
import com.example.projektopgave1.Model.UseCases.UseCaseListeOversigt.AppointmentListItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ListeOversigtController {

    @FXML private Button todayButton;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label currentDateLabel;
    @FXML private ComboBox<String> customerBookingsComboBox;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Button clearButton;
    @FXML private CheckBox allBookingsCheckBox;
    @FXML private CheckBox jonCheckBox;
    @FXML private CheckBox joachimCheckBox;
    @FXML private CheckBox lasseCheckBox;
    @FXML private CheckBox gabrielCheckBox;
    @FXML private TableView<AppointmentListItem> bookingsTableView;
    @FXML private TableColumn<AppointmentListItem, Integer> idColumn;
    @FXML private TableColumn<AppointmentListItem, String> dateColumn;
    @FXML private TableColumn<AppointmentListItem, String> startTimeColumn;
    @FXML private TableColumn<AppointmentListItem, String> endTimeColumn;
    @FXML private TableColumn<AppointmentListItem, String> customerColumn;
    @FXML private TableColumn<AppointmentListItem, String> employeeColumn;
    @FXML private TableColumn<AppointmentListItem, String> treatmentColumn;
    @FXML private TableColumn<AppointmentListItem, String> statusColumn;
    @FXML private Button newBookingButton;
    @FXML private Button editButton;
    @FXML private Button cancelBookingButton;
    @FXML private Button exportButton;
    @FXML private ToggleButton dayToggle;
    @FXML private ToggleButton weekToggle;
    @FXML private ToggleButton monthToggle;
    @FXML private ToggleButton yearToggle;

    private UseCaseListeOversigt useCaseListeOversigt;
    private ObservableList<AppointmentListItem> appointmentsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {

            LoggerUtility.logEvent("Initialiserer listeoversigt controller");
            useCaseListeOversigt = new UseCaseListeOversigt();

            setupViewComboBox();

            setupTableView();

            setupEventHandlers();

            setupEmployeeCheckboxes();

            updateDateLabel();

            loadAppointments();

            LoggerUtility.logEvent("Listeoversigt initialiseret");
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved initialisering af listeoversigt: " + e.getMessage());
        }
    }

    private void setupViewComboBox() {
        customerBookingsComboBox.setOnAction(event -> {
            String selectedView = customerBookingsComboBox.getValue();
            if (selectedView == null) return;

            try {
                if (selectedView.equals("Kalender Oversigt")) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projektopgave1/KalenderOversigt.fxml"));
                    Parent root = loader.load();

                    Scene currentScene = customerBookingsComboBox.getScene();
                    Stage stage = (Stage) currentScene.getWindow();

                    Scene newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
                    stage.setScene(newScene);

                    LoggerUtility.logEvent("Skiftet til kalendervisning");
                }
            } catch (IOException e) {
                LoggerUtility.logError("Fejl ved skift mellem visninger: " + e.getMessage());
            }
        });
    }

    private void handleViewChange(String selectedView) {
        try {
            if (selectedView == null) return;

            String fxmlPath = null;

            switch (selectedView) {
                case "Kunde/Booking oversigt":
                    clearFilters();
                    return;

                case "Alle Kunder":
                    showInfoAlert("Navigation", null, "Kunde-visning er ikke implementeret endnu.");
                    return;

                case "Alle Bookinger":
                case "Aktive Bookinger":
                    if (selectedView.equals("Aktive Bookinger")) {
                        if (filterComboBox.getItems().contains("Aktiv")) {
                            filterComboBox.setValue("Aktiv");
                        }
                    } else {
                        filterComboBox.setValue(null);
                    }
                    loadAppointments();
                    return;

                default:
                    return;
            }

        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved skift af visning: " + e.getMessage());
        }
    }

    private void setupTableView() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        employeeColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        treatmentColumn.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        bookingsTableView.setItems(appointmentsList);

        bookingsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = (newSelection != null);
            editButton.setDisable(!hasSelection);
            cancelBookingButton.setDisable(!hasSelection);
        });
    }

    private void setupEventHandlers() {

        searchField.textProperty().addListener((obs, oldValue, newValue) -> handleSearchFieldChanged());
        filterComboBox.setOnAction(event -> handleFilterChanged());
        clearButton.setOnAction(event -> handleClearButtonClick());

        allBookingsCheckBox.setOnAction(event -> handleAllCheckboxChanged());
        jonCheckBox.setOnAction(event -> handleEmployeeCheckboxChanged());
        joachimCheckBox.setOnAction(event -> handleEmployeeCheckboxChanged());
        lasseCheckBox.setOnAction(event -> handleEmployeeCheckboxChanged());
        gabrielCheckBox.setOnAction(event -> handleEmployeeCheckboxChanged());

        if (dayToggle != null) dayToggle.setOnAction(event -> handleViewToggleChanged());
        if (weekToggle != null) weekToggle.setOnAction(event -> handleViewToggleChanged());
        if (monthToggle != null) monthToggle.setOnAction(event -> handleViewToggleChanged());
        if (yearToggle != null) yearToggle.setOnAction(event -> handleViewToggleChanged());

        newBookingButton.setOnAction(event -> handleNewBookingButton());
        editButton.setOnAction(event -> handleEditButton());
        cancelBookingButton.setOnAction(event -> handleCancelBookingButton());

        editButton.setDisable(true);
        cancelBookingButton.setDisable(true);
    }

    private void handleViewToggleChanged() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projektopgave1/KalenderOversigt.fxml"));
            Parent root = loader.load();

            Scene currentScene = bookingsTableView.getScene();
            Stage stage = (Stage) currentScene.getWindow();

            Scene newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            stage.setScene(newScene);

            LoggerUtility.logEvent("Skiftet til kalendervisning");
        } catch (IOException e) {
            LoggerUtility.logError("Fejl ved skift til kalendervisning: " + e.getMessage());
        }
    }

    private void setupEmployeeCheckboxes() {
        allBookingsCheckBox.setSelected(true);
        updateEmployeeCheckboxState();
    }

    private void updateEmployeeCheckboxState() {
        boolean allSelected = allBookingsCheckBox.isSelected();
        jonCheckBox.setDisable(allSelected);
        joachimCheckBox.setDisable(allSelected);
        lasseCheckBox.setDisable(allSelected);
        gabrielCheckBox.setDisable(allSelected);

        if (allSelected) {
            jonCheckBox.setSelected(true);
            joachimCheckBox.setSelected(true);
            lasseCheckBox.setSelected(true);
            gabrielCheckBox.setSelected(true);
        }
    }

    private void updateDateLabel() {
        currentDateLabel.setText(useCaseListeOversigt.getFormattedDate());
    }

    private void loadAppointments() {
        try {
            appointmentsList.clear();

            LoggerUtility.logEvent("Henter aftaler: '" + searchField.getText() + "'");

            String searchTerm = searchField.getText();
            useCaseListeOversigt.setSearchTerm(searchTerm);

            List<String> selectedEmployees = getSelectedEmployees();
            useCaseListeOversigt.setSelectedEmployees(selectedEmployees);

            List<AppointmentListItem> appointments = useCaseListeOversigt.searchAppointments();
            LoggerUtility.logEvent("Fandt " + appointments.size() + " aftaler");

            appointmentsList.addAll(appointments);

            bookingsTableView.getSelectionModel().clearSelection();
            editButton.setDisable(true);
            cancelBookingButton.setDisable(true);
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved indlæsning af aftaler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<String> getSelectedEmployees() {
        List<String> selectedEmployees = new ArrayList<>();

        if (allBookingsCheckBox.isSelected()) {
            return selectedEmployees;
        }

        if (jonCheckBox.isSelected()) {
            selectedEmployees.add("Jon");
        }
        if (joachimCheckBox.isSelected()) {
            selectedEmployees.add("Joachim");
        }
        if (lasseCheckBox.isSelected()) {
            selectedEmployees.add("Lasse");
        }
        if (gabrielCheckBox.isSelected()) {
            selectedEmployees.add("Gabriel");
        }

        return selectedEmployees;
    }

    private void clearFilters() {
        searchField.clear();
        filterComboBox.getSelectionModel().clearSelection();
        allBookingsCheckBox.setSelected(true);
        updateEmployeeCheckboxState();
        useCaseListeOversigt.clearFilters();
        loadAppointments();
    }

    @FXML
    private void handleSearchFieldChanged() {
        loadAppointments();
    }

    @FXML
    private void handleFilterChanged() {
        loadAppointments();
    }

    @FXML
    private void handleAllCheckboxChanged() {
        updateEmployeeCheckboxState();
        loadAppointments();
    }

    @FXML
    private void handleEmployeeCheckboxChanged() {
        loadAppointments();
    }

    @FXML
    private void handleClearButtonClick() {
        clearFilters();
    }

    @FXML
    private void handleNewBookingButton() {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ny Booking");
            alert.setHeaderText("Opret ny booking");
            alert.setContentText("Her ville du normalt se en dialog til at oprette en ny booking.");
            alert.showAndWait();

            loadAppointments();
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved håndtering af ny booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditButton() {
        try {
            AppointmentListItem selectedAppointment = bookingsTableView.getSelectionModel().getSelectedItem();
            if (selectedAppointment == null) return;

            UseCaseCalendar.AppointmentData appointmentData = useCaseListeOversigt.getAppointmentById(selectedAppointment.getId());
            if (appointmentData == null) {
                LoggerUtility.logError("Kunne ikke finde aftale detaljer for ID: " + selectedAppointment.getId());
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Rediger Booking");
            dialog.setHeaderText("Rediger booking #" + selectedAppointment.getId());

            ButtonType saveButtonType = new ButtonType("Gem", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // We need to create a separate UseCaseCalendar to access these methods
            UseCaseCalendar calendarUseCase = new UseCaseCalendar();

            // Customer ComboBox with database values
            ComboBox<String> customerCombo = new ComboBox<>();
            customerCombo.getItems().addAll(calendarUseCase.getAllCustomerNames());
            customerCombo.setValue(appointmentData.getCustomerName());

            // Treatment ComboBox with database values
            ComboBox<String> treatmentCombo = new ComboBox<>();
            treatmentCombo.getItems().addAll(calendarUseCase.getAllTreatmentNames());
            treatmentCombo.setValue(appointmentData.getTreatment());

            // Employee ComboBox with database values
            ComboBox<String> employeeCombo = new ComboBox<>();
            employeeCombo.getItems().addAll(calendarUseCase.getAllEmployeeNames());
            employeeCombo.setValue(appointmentData.getEmployee());

            DatePicker datePicker = new DatePicker(appointmentData.getDate());

            ComboBox<String> startTimeCombo = new ComboBox<>();
            ComboBox<String> endTimeCombo = new ComboBox<>();

            // Populate time ComboBoxes
            for (int hour = 8; hour <= 18; hour++) {
                startTimeCombo.getItems().add(String.format("%02d:00", hour));
                endTimeCombo.getItems().add(String.format("%02d:00", hour));

                if (hour < 18) {
                    startTimeCombo.getItems().add(String.format("%02d:30", hour));
                    endTimeCombo.getItems().add(String.format("%02d:30", hour));
                }
            }

            startTimeCombo.setValue(appointmentData.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            endTimeCombo.setValue(appointmentData.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));

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

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == saveButtonType) {
                String startTimeStr = startTimeCombo.getValue();
                String endTimeStr = endTimeCombo.getValue();
                LocalTime startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
                LocalDate selectedDate = datePicker.getValue();

                useCaseListeOversigt.updateAppointment(
                        selectedAppointment.getId(),
                        customerCombo.getValue(),
                        treatmentCombo.getValue(),
                        employeeCombo.getValue(),
                        selectedDate,
                        startTime,
                        endTime
                );

                loadAppointments();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Booking Opdateret");
                alert.setHeaderText(null);
                alert.setContentText("Booking #" + selectedAppointment.getId() + " er blevet opdateret.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved håndtering af rediger booking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelBookingButton() {
        try {
            AppointmentListItem selectedAppointment = bookingsTableView.getSelectionModel().getSelectedItem();
            if (selectedAppointment == null) return;

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Bekræft Annullering");
            confirmAlert.setHeaderText("Er du sikker på, at du vil annullere denne booking?");
            confirmAlert.setContentText("Booking #" + selectedAppointment.getId());

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                useCaseListeOversigt.cancelAppointment(selectedAppointment.getId());

                LoggerUtility.logEvent("Genindlæser bookinger efter annullering");
                loadAppointments();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Booking Annulleret");
                alert.setHeaderText(null);
                alert.setContentText("Booking #" + selectedAppointment.getId() + " er blevet annulleret.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved annullering af booking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showInfoAlert(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}