package com.example.projektopgave1.Controller;

import com.example.projektopgave1.Model.DatabaseHandlers.KundeDatabaseHandler;
import com.example.projektopgave1.Model.Entiteter.Kunde;
import com.example.projektopgave1.Model.UseCases.UseCaseOpretBooking;
import Utils.LoggerUtility;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OpretBookingController {
    @FXML private ComboBox<String> customerComboBox;
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private ComboBox<String> treatmentComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> startTimeComboBox;
    @FXML private Button saveBookingButton;
    @FXML private Button cancelButton;
    @FXML private Label treatmentDurationLabel;
    @FXML private Label treatmentPriceLabel;
    @FXML private Label treatmentEndTimeLabel;

    @FXML private TextField nameTextField;
    @FXML private TextField phoneTextField;
    @FXML private TextField emailTextField;
    @FXML private TextField addressTextField;
    @FXML private Button saveCustomerButton;

    // Use case og database handler vi skal bruge
    private UseCaseOpretBooking useCaseOpretBooking;
    private KundeDatabaseHandler kundeDatabaseHandler;

    private Stage dialogStage;

    // Holder styr på om brugeren har gemt
    private boolean saveClicked = false;

    // Initialize metoden kører så snart vores .fxml er loadet
    @FXML
    public void initialize() {
        try {
            LoggerUtility.logEvent("Starter OpretBookingController");

            // Opreter use case og database handler
            useCaseOpretBooking = new UseCaseOpretBooking();
            kundeDatabaseHandler = new KundeDatabaseHandler();

            // Fylder alle comboboxes med data
            fyldKundeComboBox();
            fyldMedarbejderComboBox();
            fyldBehandlingComboBox();
            fyldTidsComboBox();

            // Tilføj listener til behandling så vi kan vise info
            tilføjBehandlingsListener();

            // Sætter datoen til i dag som standard
            datePicker.setValue(LocalDate.now());

            // Fokusere på kunde feltet når vinduet åbner
            Platform.runLater(() -> customerComboBox.requestFocus());

            LoggerUtility.logEvent("OpretBookingController klar");
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved opstart af booking vindue: " + e.getMessage());
        }
    }

     // Henter alle kunder fra databasen og putter dem i comboboxen
    private void fyldKundeComboBox() {
        try {
            // Rydder comboboxen først
            customerComboBox.getItems().clear();

            // Hent alle kunder fra databasen
            List<Kunde> kunder = kundeDatabaseHandler.readAll();
            LoggerUtility.logEvent("Hentede " + kunder.size() + " kunder fra databasen");

            // Tilføj hver kunde til comboboxen
            for (Kunde kunde : kunder) {
                customerComboBox.getItems().add(kunde.getNavn());
            }

            LoggerUtility.logEvent("Kunde combobox fyldt med " + customerComboBox.getItems().size() + " kunder");
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved indlæsning af kunder: " + e.getMessage());
        }
    }

    private void tilføjBehandlingsListener() {
        treatmentComboBox.setOnAction(event -> {
            String valgtBehandling = treatmentComboBox.getValue();
            if (valgtBehandling != null) {
                opdaterBehandlingsInfo(valgtBehandling);
            }
        });
    }

    private void opdaterBehandlingsInfo(String behandlingsNavn) {
        try {
            // Henter varigheden for behandlingen
            int varighed = useCaseOpretBooking.getTreatmentDuration(behandlingsNavn);

            // Viser varigheden
            treatmentDurationLabel.setText("Varighed: " + varighed + " minutter");

            // Beregner sluttidspunkt hvis der er valgt starttid
            String startTidStr = startTimeComboBox.getValue();
            if (startTidStr != null) {
                LocalTime startTid = LocalTime.parse(startTidStr, DateTimeFormatter.ofPattern("HH:mm"));
                LocalTime slutTid = startTid.plusMinutes(varighed);
                treatmentEndTimeLabel.setText("Forventet sluttidspunkt: " + slutTid.format(DateTimeFormatter.ofPattern("HH:mm")));
            }

            // Her kunne vi måske have lavet en metode der henter prisen fra databasen til hver behandling.
            // I stedet kan kunden selv spørge :D

            treatmentPriceLabel.setText("Pris: Spørg din frisør");

        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved visning af behandlingsinfo: " + e.getMessage());
        }
    }

    // Denne funktion fylder combobox ud med medarbejdere der er oprettet i databasen
    private void fyldMedarbejderComboBox() {
        employeeComboBox.getItems().addAll(useCaseOpretBooking.getAllEmployeeNames());
    }

    // Denne funktion fylder comboboxen for behandlinger vi har defineret i databasen.
    private void fyldBehandlingComboBox() {
        treatmentComboBox.getItems().addAll(useCaseOpretBooking.getAllTreatmentNames());
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    // Denne funktion fylder comboboxen for tider på dagen. Alle mellem tidspunkterne 08-18
    private void fyldTidsComboBox() {
        for (int time = 8; time <= 18; time++) {
            startTimeComboBox.getItems().add(String.format("%02d:00", time));
            if (time < 18) { // Tilføj ikke 18:30 da det er efter lukketid
                startTimeComboBox.getItems().add(String.format("%02d:30", time));
            }
        }
    }

    // Denne funktion kører når vi trykker på Gem Booking
    @FXML
    private void handleSaveBooking() {
        // Henter alle værdier fra felterne
        String kunde = customerComboBox.getValue();
        String medarbejder = employeeComboBox.getValue();
        String behandling = treatmentComboBox.getValue();
        LocalDate dato = datePicker.getValue();
        String startTidStr = startTimeComboBox.getValue();

        // Tjek at alle felter er udfyldt
        if (kunde == null || medarbejder == null || behandling == null || dato == null || startTidStr == null) {
            visAlert("Fejl", "Alle felter skal udfyldes.");
            return;
        }

        // Konverter starttid fra string til LocalTime
        LocalTime startTid;
        try {
            startTid = LocalTime.parse(startTidStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            visAlert("Fejl", "Ugyldigt tidformat.");
            return;
        }

        // Beregn sluttiden baseret på behandlingens varighed defineret i vores database
        int varighed = useCaseOpretBooking.getTreatmentDuration(behandling);
        LocalTime slutTid = startTid.plusMinutes(varighed);

        // Tjekker om tidspunktet er ledigt
        if (!useCaseOpretBooking.isTimeSlotAvailable(medarbejder, dato, startTid, slutTid)) {
            visAlert("Fejl", "Tid er ikke tilgængelig for " + medarbejder);
            return;
        }

        // Prøver at oprette bookingen
        boolean success = useCaseOpretBooking.createBooking(kunde, behandling, medarbejder, dato, startTid, slutTid);

        if (success) {
            saveClicked = true;
            visAlert("Succes", "Booking oprettet.");
            if (dialogStage != null) {
                dialogStage.close();
            }
        } else {
            visAlert("Fejl", "Booking kunne ikke oprettes.");
        }
    }

    // Denne funktion kører når der bliver trykket på Gem Kunde
    @FXML
    private void handleSaveCustomer() {
        // Tjek at navnet er udfyldt
        if (nameTextField == null || nameTextField.getText() == null || nameTextField.getText().trim().isEmpty()) {
            visAlert("Fejl", "Kundenavn skal udfyldes.");
            return;
        }

        try {
            // Opret et nyt kunde objekt med de indtastede data
            Kunde nyKunde = new Kunde(
                    0,
                    nameTextField.getText().trim(),
                    phoneTextField.getText() != null ? phoneTextField.getText().trim() : "",
                    emailTextField.getText() != null ? emailTextField.getText().trim() : "",
                    addressTextField.getText() != null ? addressTextField.getText().trim() : ""
            );

            // Gemmer kunden i databasen
            Kunde gemt = kundeDatabaseHandler.create(nyKunde);

            if (gemt != null && gemt.getKundeID() > 0) {
                // Opdatere kundelisten så den nye kunde vises
                fyldKundeComboBox();

                // Vælg den nye kunde i listen
                customerComboBox.setValue(gemt.getNavn());

                // Skift tilbage til første fane
                TabPane tabPane = (TabPane) nameTextField.getScene().lookup("#tabPane");
                if (tabPane != null) {
                    tabPane.getSelectionModel().select(0);
                }

                visAlert("Succes", "Kunde oprettet og tilføjet.");
            } else {
                visAlert("Fejl", "Kunde kunne ikke oprettes.");
            }
        } catch (Exception e) {
            LoggerUtility.logError("Fejl ved oprettelse af kunde: " + e.getMessage());
            visAlert("Fejl", "Der opstod en fejl: " + e.getMessage());
        }
    }

    // Her tjekker vi om brugeren trykker på annuler
    @FXML
    private void handleCancel() {
        saveClicked = false;
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // En standard AlertBoks
    private void visAlert(String titel, String besked) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titel);
        alert.setContentText(besked);
        alert.showAndWait();
    }
}