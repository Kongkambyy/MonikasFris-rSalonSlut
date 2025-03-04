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

    private LocalDate currentDate = LocalDate.now();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private String searchTerm = "";
    private List<String> selectedEmployees = new ArrayList<>();

    private final UseCaseCalendar calendarUseCase;
    private final AftaleDatabaseHandler aftaleDatabaseHandler;
    private final KundeDatabaseHandler kundeDatabaseHandler;
    private final MedarbejderDatabaseHandler medarbejderDatabaseHandler;
    private final BehandlingDatabaseHandler behandlingDatabaseHandler;

    public UseCaseListeOversigt() {
        this(new AftaleDatabaseHandler(), new KundeDatabaseHandler(), new MedarbejderDatabaseHandler(), new BehandlingDatabaseHandler());
    }

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

    public static class AppointmentListItem {
        private final int id;
        private final String date;
        private final String startTime;
        private final String endTime;
        private final String customerName;
        private final String employeeName;
        private final String treatment;
        private final String status;

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

        public int getId() { return id; }
        public String getDate() { return date; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getCustomerName() { return customerName; }
        public String getEmployeeName() { return employeeName; }
        public String getTreatment() { return treatment; }
        public String getStatus() { return status; }
    }

    public static class AppointmentData {
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

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String term) {
        searchTerm = term != null ? term.toLowerCase() : "";
    }

    public String getFormattedDate() {
        return currentDate.format(dateFormatter);
    }

    public void setSelectedEmployees(List<String> employees) {
        selectedEmployees.clear();
        if (employees != null) selectedEmployees.addAll(employees);
    }

    public void clearFilters() {
        searchTerm = "";
        selectedEmployees.clear();
    }

    public List<AppointmentData> getAllAppointments() {
        List<AppointmentData> appointments = new ArrayList<>();
        try {
            List<Aftale> alleAftaler = aftaleDatabaseHandler.getAll();
            for (Aftale aftale : alleAftaler) {
                try {
                    Kunde kunde = kundeDatabaseHandler.readById(aftale.getKundeID());
                    String kundeNavn = kunde != null ? kunde.getNavn() : "Ukendt";
                    String kundeId = kunde != null ? String.valueOf(kunde.getKundeID()) : "";
                    Medarbejder medarbejder = medarbejderDatabaseHandler.getById(aftale.getMedarbejderID());
                    String medarbejderNavn = medarbejder != null ? medarbejder.getNavn() : "Ukendt";
                    int medarbejderId = medarbejder != null ? medarbejder.getMedarbejderID() : 0;
                    Behandling behandling = behandlingDatabaseHandler.getById(aftale.getBehandlingID());
                    String behandlingNavn = behandling != null ? behandling.getBehandling() : "Ukendt";
                    int behandlingId = behandling != null ? behandling.getBehandlingID() : 0;
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
                    LoggerUtility.logError("Fejl under konvertering af aftale " + aftale.getAftaleID() + ": " + e.getMessage());
                }
            }
        } catch (DatabaseConnectionException e) {
            LoggerUtility.logError("Fejl ved hentning af aftaler: " + e.getMessage());
        }
        return appointments;
    }

    public List<AppointmentListItem> searchAppointments() {
        List<AppointmentListItem> result = new ArrayList<>();
        try {
            List<Aftale> appointments = aftaleDatabaseHandler.getAll();
            LoggerUtility.logEvent("Henter alle aftaler. Antal: " + appointments.size());

            for (Aftale aftale : appointments) {
                try {
                    if (!searchTerm.isEmpty() || !selectedEmployees.isEmpty()) {
                        Kunde kunde = kundeDatabaseHandler.readById(aftale.getKundeID());
                        Medarbejder medarbejder = medarbejderDatabaseHandler.getById(aftale.getMedarbejderID());

                        if (!selectedEmployees.isEmpty() && medarbejder != null && !selectedEmployees.contains(medarbejder.getNavn())) {
                            continue;
                        }

                        if (!searchTerm.isEmpty()) {
                            boolean matches = false;
                            if ((kunde != null && kunde.getNavn().toLowerCase().contains(searchTerm)) ||
                                    (medarbejder != null && medarbejder.getNavn().toLowerCase().contains(searchTerm)) ||
                                    aftale.getStatus().toLowerCase().contains(searchTerm)) {
                                matches = true;
                            }

                            if (!matches) {
                                continue;
                            }
                        }
                    }

                    AppointmentListItem item = convertToListItem(aftale);
                    if (item != null) result.add(item);

                } catch (Exception e) {
                    LoggerUtility.logError("Fejl under filtrering af aftale " + aftale.getAftaleID() + ": " + e.getMessage());
                }
            }
        } catch (DatabaseConnectionException e) {
            LoggerUtility.logError("Fejl ved søgning efter aftaler: " + e.getMessage());
        }
        return result;
    }

    public void createAppointment(String customerName, String treatment, String employee,
                                  LocalDate date, LocalTime startTime, LocalTime endTime) {
        calendarUseCase.createNewAppointment(customerName, treatment, employee, startTime, endTime, date);
    }

    public void updateAppointment(int appointmentId, String customerName, String treatment, String employee,
                                  LocalDate date, LocalTime startTime, LocalTime endTime) {
        calendarUseCase.updateAppointment(appointmentId, customerName, treatment, employee, startTime, endTime, date);
    }

    public void cancelAppointment(int appointmentId) {
        calendarUseCase.deleteAppointment(appointmentId);
    }

    public UseCaseCalendar.AppointmentData getAppointmentById(int appointmentId) {
        return calendarUseCase.getAppointmentById(appointmentId);
    }

    private AppointmentListItem convertToListItem(Aftale aftale) {
        try {
            Kunde kunde = kundeDatabaseHandler.readById(aftale.getKundeID());
            Medarbejder medarbejder = medarbejderDatabaseHandler.getById(aftale.getMedarbejderID());
            Behandling behandling = behandlingDatabaseHandler.getById(aftale.getBehandlingID());
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
            LoggerUtility.logError("Fejl ved konvertering af aftale: " + e.getMessage());
            return null;
        }
    }

    private AppointmentListItem createListItem(int id, LocalDate date, LocalTime startTime, LocalTime endTime,
                                               String customerName, String employeeName, String treatment, String status) {
        return new AppointmentListItem(
                id,
                date.format(dateFormatter),
                startTime.format(TIME_FORMATTER),
                endTime.format(TIME_FORMATTER),
                customerName,
                employeeName,
                treatment,
                status
        );
    }

    public List<String> getAllEmployeeNames() {
        return calendarUseCase.getAllEmployeeNames();
    }
}
