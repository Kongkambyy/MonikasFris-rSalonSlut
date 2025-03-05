package com.example.projektopgave1.Model.UseCases;

import Utils.LoggerUtility;
import com.example.projektopgave1.CustomExceptions.DatabaseConnectionException;
import com.example.projektopgave1.Model.DatabaseHandlers.AftaleDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.BehandlingDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.KundeDatabaseHandler;
import com.example.projektopgave1.Model.DatabaseHandlers.MedarbejderDatabaseHandler;
import com.example.projektopgave1.Model.Entiteter.Aftale;
import com.example.projektopgave1.Model.Entiteter.Behandling;
import com.example.projektopgave1.Model.Entiteter.Kunde;
import com.example.projektopgave1.Model.Entiteter.Medarbejder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UseCaseListeOversigt {

    // Aktuel dato, brugt til at filtrere aftaler i listevisningen
    private LocalDate currentDate = LocalDate.now();
    // Dato-formatter til at vise datoer i dansk format (dd-mm-åååå)
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    // Tids-formatter til at vise tidspunkter i 24-timers format
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    // Søgeterm, der bruges til at filtrere aftaler
    private String searchTerm = "";
    // Liste over valgte medarbejdere til filtrering
    private List<String> selectedEmployees = new ArrayList<>();

    // Reference til UseCaseCalendar for at genbruge funktionalitet
    private final UseCaseCalendar calendarUseCase;
    // Database handlers til at kommunikere med de forskellige tabeller
    private final AftaleDatabaseHandler aftaleDatabaseHandler;
    private final KundeDatabaseHandler kundeDatabaseHandler;
    private final MedarbejderDatabaseHandler medarbejderDatabaseHandler;
    private final BehandlingDatabaseHandler behandlingDatabaseHandler;

    // Standard konstruktør, der opretter alle nødvendige database handlers
    public UseCaseListeOversigt() {
        this(new AftaleDatabaseHandler(), new KundeDatabaseHandler(), new MedarbejderDatabaseHandler(), new BehandlingDatabaseHandler());
    }

    // Konstruktør der tillader dependency injection for bedre testbarhed
    public UseCaseListeOversigt(AftaleDatabaseHandler aftaleDatabaseHandler,
                                KundeDatabaseHandler kundeDatabaseHandler,
                                MedarbejderDatabaseHandler medarbejderDatabaseHandler,
                                BehandlingDatabaseHandler behandlingDatabaseHandler) {
        this.aftaleDatabaseHandler = aftaleDatabaseHandler;
        this.kundeDatabaseHandler = kundeDatabaseHandler;
        this.medarbejderDatabaseHandler = medarbejderDatabaseHandler;
        this.behandlingDatabaseHandler = behandlingDatabaseHandler;
        this.calendarUseCase = new UseCaseCalendar();
    }

    // Indre klasse til at repræsentere en aftale i listevisningen
    public static class AppointmentListItem {
        // Aftale-information formateret til visning i TableView
        private final int id;
        private final String date;
        private final String startTime;
        private final String endTime;
        private final String customerName;
        private final String employeeName;
        private final String treatment;
        private final String status;

        // Konstruktør til at oprette et nyt listevisningselement
        public AppointmentListItem(int id, String date, String startTime, String endTime,
                                   String customerName, String employeeName, String treatment, String status) {
            this.id = id;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.customerName = customerName;
            this.employeeName = employeeName;
            this.treatment = treatment;
            this.status = status;
        }

        // Get-metoder til brug i TableView via PropertyValueFactory
        public int getId() { return id; }
        public String getDate() { return date; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getCustomerName() { return customerName; }
        public String getEmployeeName() { return employeeName; }
        public String getTreatment() { return treatment; }
        public String getStatus() { return status; }
    }

    // Indre klasse til at holde detaljeret information om en aftale
    public static class AppointmentData {
        // Detaljeret aftale-information inkl. ID'er til relationelle data
        private int id;
        private String customerId;
        private String customerName;
        private int employeeId;
        private String employeeName;
        private int treatmentId;
        private String treatmentName;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String status;

        // Konstruktør til at oprette et detaljeret aftaleobjekt
        public AppointmentData(int id, String customerId, String customerName, int employeeId, String employeeName,
                               int treatmentId, String treatmentName, LocalDate date, LocalTime startTime, LocalTime endTime, String status) {
            this.id = id;
            this.customerId = customerId;
            this.customerName = customerName;
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.treatmentId = treatmentId;
            this.treatmentName = treatmentName;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.status = status;
        }

        // Get-metoder til at tilgå alle aftale-oplysninger
        public int getId() { return id; }
        public String getCustomerId() { return customerId; }
        public String getCustomerName() { return customerName; }
        public int getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public int getTreatmentId() { return treatmentId; }
        public String getTreatmentName() { return treatmentName; }
        public LocalDate getDate() { return date; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public String getStatus() { return status; }
    }

    // Returnerer den aktuelle dato
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    // Henter den aktuelle søgeterm
    public String getSearchTerm() {
        return searchTerm;
    }

    // Sætter en ny søgeterm (konverterer til lowercase for case-insensitive søgning)
    public void setSearchTerm(String term) {
        searchTerm = term != null ? term.toLowerCase() : "";
    }

    // Returnerer den aktuelle dato formateret som tekst
    public String getFormattedDate() {
        return currentDate.format(dateFormatter);
    }

    // Opdaterer listen over valgte medarbejdere til filtrering
    public void setSelectedEmployees(List<String> employees) {
        selectedEmployees.clear();
        if (employees != null) selectedEmployees.addAll(employees);
    }

    // Nulstiller alle filtre (søgeterm og medarbejdervalg)
    public void clearFilters() {
        searchTerm = "";
        selectedEmployees.clear();
    }

    // Henter alle aftaler med fyldig information
    public List<AppointmentData> getAllAppointments() {
        List<AppointmentData> appointments = new ArrayList<>();
        try {
            // Hent alle aftaler fra databasen
            List<Aftale> alleAftaler = aftaleDatabaseHandler.getAll();
            for (Aftale aftale : alleAftaler) {
                try {
                    // Hent relaterede data for hver aftale
                    Kunde kunde = kundeDatabaseHandler.readById(aftale.getKundeID());
                    String kundeNavn = kunde != null ? kunde.getNavn() : "Ukendt";
                    String kundeId = kunde != null ? String.valueOf(kunde.getKundeID()) : "";

                    Medarbejder medarbejder = medarbejderDatabaseHandler.getById(aftale.getMedarbejderID());
                    String medarbejderNavn = medarbejder != null ? medarbejder.getNavn() : "Ukendt";
                    int medarbejderId = medarbejder != null ? medarbejder.getMedarbejderID() : 0;

                    Behandling behandling = behandlingDatabaseHandler.getById(aftale.getBehandlingID());
                    String behandlingNavn = behandling != null ? behandling.getBehandling() : "Ukendt";
                    int behandlingId = behandling != null ? behandling.getBehandlingID() : 0;

                    // Opret et AppointmentData objekt med alle detaljer
                    AppointmentData appointmentData = new AppointmentData(
                            aftale.getAftaleID(),
                            kundeId,
                            kundeNavn,
                            medarbejderId,
                            medarbejderNavn,
                            behandlingId,
                            behandlingNavn,
                            aftale.getStarttidspunkt().toLocalDate(),
                            aftale.getStarttidspunkt().toLocalTime(),
                            aftale.getSluttidspunkt().toLocalTime(),
                            aftale.getStatus()
                    );
                    appointments.add(appointmentData);
                } catch (Exception e) {
                    // Log fejl for denne aftale, men fortsæt med de andre
                    LoggerUtility.logError("Fejl under konvertering af aftale " + aftale.getAftaleID() + ": " + e.getMessage());
                }
            }
        } catch (DatabaseConnectionException e) {
            // Log fejl hvis databaseforbindelsen fejler
            LoggerUtility.logError("Fejl ved hentning af aftaler: " + e.getMessage());
        }
        return appointments;
    }

    // Søger efter aftaler baseret på søgeterm og medarbejderfiltre
    public List<AppointmentListItem> searchAppointments() {
        List<AppointmentListItem> result = new ArrayList<>();
        try {
            // Hent alle aftaler fra databasen
            List<Aftale> appointments = aftaleDatabaseHandler.getAll();
            LoggerUtility.logEvent("Henter alle aftaler. Antal: " + appointments.size());

            for (Aftale aftale : appointments) {
                try {
                    // Hvis der er aktive filtre, tjek om aftalen matcher
                    if (!searchTerm.isEmpty() || !selectedEmployees.isEmpty()) {
                        Kunde kunde = kundeDatabaseHandler.readById(aftale.getKundeID());
                        Medarbejder medarbejder = medarbejderDatabaseHandler.getById(aftale.getMedarbejderID());

                        // Filtrer efter valgte medarbejdere
                        if (!selectedEmployees.isEmpty() && medarbejder != null && !selectedEmployees.contains(medarbejder.getNavn())) {
                            continue; // Spring over hvis medarbejderen ikke er valgt
                        }

                        // Filtrer efter søgeterm
                        if (!searchTerm.isEmpty()) {
                            boolean matches = false;
                            if ((kunde != null && kunde.getNavn().toLowerCase().contains(searchTerm)) ||
                                    (medarbejder != null && medarbejder.getNavn().toLowerCase().contains(searchTerm)) ||
                                    aftale.getStatus().toLowerCase().contains(searchTerm)) {
                                matches = true;
                            }

                            if (!matches) {
                                continue; // Spring over hvis aftalen ikke matcher søgetermen
                            }
                        }
                    }

                    // Konverter til listevisningselement og tilføjer til resultatet
                    AppointmentListItem item = convertToListItem(aftale);
                    if (item != null) result.add(item);

                } catch (Exception e) {
                    // Log fejl for denne aftale, men fortsæt med de andre
                    LoggerUtility.logError("Fejl under filtrering af aftale " + aftale.getAftaleID() + ": " + e.getMessage());
                }
            }
        } catch (DatabaseConnectionException e) {
            // Log fejl hvis databaseforbindelsen fejler
            LoggerUtility.logError("Fejl ved søgning efter aftaler: " + e.getMessage());
        }
        return result;
    }

    // Opretter en ny aftale (via UseCaseCalendar)
    public void createAppointment(String customerName, String treatment, String employee,
                                  LocalDate date, LocalTime startTime, LocalTime endTime) {
        calendarUseCase.createNewAppointment(customerName, treatment, employee, startTime, endTime, date);
    }

    // Opdaterer en eksisterende aftale (via UseCaseCalendar)
    public void updateAppointment(int appointmentId, String customerName, String treatment, String employee,
                                  LocalDate date, LocalTime startTime, LocalTime endTime) {
        calendarUseCase.updateAppointment(appointmentId, customerName, treatment, employee, startTime, endTime, date);
    }

    // Annullerer en aftale (via UseCaseCalendar)
    public void cancelAppointment(int appointmentId) {
        calendarUseCase.deleteAppointment(appointmentId);
    }

    // Henter detaljeret information om en aftale (via UseCaseCalendar)
    public UseCaseCalendar.AppointmentData getAppointmentById(int appointmentId) {
        return calendarUseCase.getAppointmentById(appointmentId);
    }

    // Konverterer en Aftale-entitet til et listevisningselement
    private AppointmentListItem convertToListItem(Aftale aftale) {
        try {
            // Hent relaterede data for aftalen
            Kunde kunde = kundeDatabaseHandler.readById(aftale.getKundeID());
            Medarbejder medarbejder = medarbejderDatabaseHandler.getById(aftale.getMedarbejderID());
            Behandling behandling = behandlingDatabaseHandler.getById(aftale.getBehandlingID());

            // Opret og returner et formateret listevisningselement
            return createListItem(
                    aftale.getAftaleID(),
                    aftale.getStarttidspunkt().toLocalDate(),
                    aftale.getStarttidspunkt().toLocalTime(),
                    aftale.getSluttidspunkt().toLocalTime(),
                    kunde != null ? kunde.getNavn() : "Ukendt",
                    medarbejder != null ? medarbejder.getNavn() : "Ukendt",
                    behandling != null ? behandling.getBehandling() : "Ukendt",
                    aftale.getStatus()
            );
        } catch (Exception e) {
            // Log fejl og returner null hvis der opstår problemer
            LoggerUtility.logError("Fejl ved konvertering af aftale: " + e.getMessage());
            return null;
        }
    }

    // Hjælpemetode til at oprette et listevisningselement med formaterede data
    private AppointmentListItem createListItem(int id, LocalDate date, LocalTime startTime, LocalTime endTime,
                                               String customerName, String employeeName, String treatment, String status) {
        return new AppointmentListItem(
                id,
                date.format(dateFormatter),  // Formatterer dato til tekst
                startTime.format(TIME_FORMATTER),  // Formatterer starttid til tekst
                endTime.format(TIME_FORMATTER),    // Formatterer sluttid til tekst
                customerName,
                employeeName,
                treatment,
                status
        );
    }

    // Henter navne på alle medarbejdere (via UseCaseCalendar)
    public List<String> getAllEmployeeNames() {
        return calendarUseCase.getAllEmployeeNames();
    }
}