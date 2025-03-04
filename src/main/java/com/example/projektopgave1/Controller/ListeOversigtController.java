package com.example.projektopgave1.Controller;

import Utils.LoggerUtility;
import com.example.projektopgave1.Model.UseCases.UseCaseListeOversigt;
import com.example.projektopgave1.Model.UseCases.UseCaseListeOversigt.AppointmentListItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
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
            // Initialize use case
            useCaseListeOversigt = new UseCaseListeOversigt();

            // Set up view combobox
            setupViewComboBox();

            // Set up table columns
            setupTableView();

            // Set up event handlers
            setupEventHandlers();

            // Set up employee checkboxes
            setupEmployeeCheckboxes();

            // Update date label
            updateDateLabel();

            // Initial load of appointments
            loadAppointments();

            LoggerUtility.logEvent("Listeoversigt initialiseret");
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved initialisering af listeoversigt: " + e.getMessage());
        }
    }

    private void setupViewComboBox() {
        // ComboBox er allerede sat op i FXML, så vi skal bare håndtere valg
        customerBookingsComboBox.setOnAction(event -> {
            String selectedView = customerBookingsComboBox.getValue();
            if (selectedView == null) return;

            try {
                if (selectedView.equals("Kalender Oversigt")) {
                    // Skift til kalendervisning
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projektopgave1/KalenderOversigt.fxml"));
                    Parent root = loader.load();

                    Scene currentScene = customerBookingsComboBox.getScene();
                    Stage stage = (Stage) currentScene.getWindow();

                    Scene newScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
                    stage.setScene(newScene);

                    LoggerUtility.logEvent("Skiftet til kalendervisning");
                }
                // Hvis "Listevisning" er valgt, er vi allerede i den rigtige visning
            } catch (IOException e) {
                LoggerUtility.logError("Fejl ved skift mellem visninger: " + e.getMessage());
            }
        });
    }

    private void handleViewChange(String selectedView) {
        try {
            if (selectedView == null) return;

            // Determine which view to show based on selection
            String fxmlPath = null;

            switch (selectedView) {
                case "Kunde/Booking oversigt":
                    // Stay in current view but reset filters
                    clearFilters();
                    return;

                case "Alle Kunder":
                    // Switch to customer view (not implemented)
                    showInfoAlert("Navigation", null, "Kunde-visning er ikke implementeret endnu.");
                    return;

                case "Alle Bookinger":
                case "Aktive Bookinger":
                    // Already in booking list view - just update filters
                    if (selectedView.equals("Aktive Bookinger")) {
                        // Only show active bookings
                        if (filterComboBox.getItems().contains("Aktiv")) {
                            filterComboBox.setValue("Aktiv");
                        }
                    } else {
                        // Show all bookings
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
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        employeeColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        treatmentColumn.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Bind observable list to table
        bookingsTableView.setItems(appointmentsList);

        // Add listener for selection changes
        bookingsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = (newSelection != null);
            editButton.setDisable(!hasSelection);
            cancelBookingButton.setDisable(!hasSelection);
        });
    }

    private void setupEventHandlers() {
        // Navigation buttons
        todayButton.setOnAction(event -> handleTodayButton());
        prevButton.setOnAction(event -> handlePreviousButton());
        nextButton.setOnAction(event -> handleNextButton());

        // Search and filter controls
        searchField.textProperty().addListener((obs, oldValue, newValue) -> handleSearchFieldChanged());
        filterComboBox.setOnAction(event -> handleFilterChanged());
        clearButton.setOnAction(event -> handleClearButtonClick());

        // Employee checkboxes
        allBookingsCheckBox.setOnAction(event -> handleAllCheckboxChanged());
        jonCheckBox.setOnAction(event -> handleEmployeeCheckboxChanged());
        joachimCheckBox.setOnAction(event -> handleEmployeeCheckboxChanged());
        lasseCheckBox.setOnAction(event -> handleEmployeeCheckboxChanged());
        gabrielCheckBox.setOnAction(event -> handleEmployeeCheckboxChanged());

        // View toggle buttons if present
        if (dayToggle != null) dayToggle.setOnAction(event -> handleViewToggleChanged());
        if (weekToggle != null) weekToggle.setOnAction(event -> handleViewToggleChanged());
        if (monthToggle != null) monthToggle.setOnAction(event -> handleViewToggleChanged());
        if (yearToggle != null) yearToggle.setOnAction(event -> handleViewToggleChanged());

        // Action buttons
        newBookingButton.setOnAction(event -> handleNewBookingButton());
        editButton.setOnAction(event -> handleEditButton());
        cancelBookingButton.setOnAction(event -> handleCancelBookingButton());
        exportButton.setOnAction(event -> handleExportButton());

        // Initially disable edit/cancel buttons until selection
        editButton.setDisable(true);
        cancelBookingButton.setDisable(true);
    }

    private void handleViewToggleChanged() {
        try {
            // Switch to calendar view if any of the toggle buttons are selected
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
        // Set initial state
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
            // Clear existing items
            appointmentsList.clear();

            // Get current search term
            String searchTerm = searchField.getText();
            useCaseListeOversigt.setSearchTerm(searchTerm);

            // Get selected employees
            List<String> selectedEmployees = getSelectedEmployees();
            useCaseListeOversigt.setSelectedEmployees(selectedEmployees);

            // Search and add results to list
            List<AppointmentListItem> appointments = useCaseListeOversigt.searchAppointments();
            appointmentsList.addAll(appointments);

            // Clear selection
            bookingsTableView.getSelectionModel().clearSelection();
            editButton.setDisable(true);
            cancelBookingButton.setDisable(true);
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved indlæsning af aftaler: " + e.getMessage());
        }
    }

    private List<String> getSelectedEmployees() {
        List<String> selectedEmployees = new ArrayList<>();

        if (allBookingsCheckBox.isSelected()) {
            return selectedEmployees; // Empty list means all employees
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
    private void handleTodayButton() {
        useCaseListeOversigt.moveToToday();
        updateDateLabel();
        loadAppointments();
    }

    @FXML
    private void handlePreviousButton() {
        useCaseListeOversigt.moveToPreviousDay();
        updateDateLabel();
        loadAppointments();
    }

    @FXML
    private void handleNextButton() {
        useCaseListeOversigt.moveToNextDay();
        updateDateLabel();
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
            // Show information dialog for now
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ny Booking");
            alert.setHeaderText("Opret ny booking");
            alert.setContentText("Her ville du normalt se en dialog til at oprette en ny booking.");
            alert.showAndWait();

            // After creating, reload appointments
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

            // Show information dialog for now
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rediger Booking");
            alert.setHeaderText("Rediger booking #" + selectedAppointment.getId());
            alert.setContentText("Her ville du normalt se en dialog til at redigere en booking.");
            alert.showAndWait();

            // After editing, reload appointments
            loadAppointments();
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved håndtering af rediger booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelBookingButton() {
        try {
            AppointmentListItem selectedAppointment = bookingsTableView.getSelectionModel().getSelectedItem();
            if (selectedAppointment == null) return;

            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Bekræft Annullering");
            confirmAlert.setHeaderText("Er du sikker på, at du vil annullere denne booking?");
            confirmAlert.setContentText("Booking #" + selectedAppointment.getId());

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Cancel booking
                useCaseListeOversigt.cancelAppointment(selectedAppointment.getId());

                // Show confirmation
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Booking Annulleret");
                alert.setHeaderText(null);
                alert.setContentText("Booking #" + selectedAppointment.getId() + " er blevet annulleret.");
                alert.showAndWait();

                // Reload appointments
                loadAppointments();
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved annullering af booking: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportButton() {
        try {
            // Get current table items
            List<AppointmentListItem> appointments = new ArrayList<>(bookingsTableView.getItems());

            if (appointments.isEmpty()) {
                showInfoAlert("Ingen data", null, "Der er ingen aftaler at eksportere.");
                return;
            }

            // Open file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Gem CSV fil");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV filer", "*.csv")
            );
            fileChooser.setInitialFileName("aftaler.csv");

            // Show save dialog
            File file = fileChooser.showSaveDialog(bookingsTableView.getScene().getWindow());
            if (file != null) {
                // Export to CSV
                boolean success = useCaseListeOversigt.exportAppointmentsToCsv(appointments, file.getAbsolutePath());

                // Show result dialog
                if (success) {
                    showInfoAlert("Eksport fuldført", null, "Aftaler er eksporteret til " + file.getName());
                } else {
                    showErrorAlert("Eksport fejlet", null, "Der opstod en fejl under eksport af aftaler.");
                }
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved eksport af aftaler: " + e.getMessage());
            showErrorAlert("Eksport fejlet", null, "Der opstod en uventet fejl: " + e.getMessage());
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